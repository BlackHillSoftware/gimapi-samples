import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.SysmodType;
import com.blackhillsoftware.gimapi.Zone;
import com.blackhillsoftware.gimapi.subentry.Smrtdata;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public class HolddataAISummary
{
    private static final String API_URL = "https://api.openai.com/v1/responses";
    private static final String DEFAULT_MODEL = "gpt-5.5";

    // Notes on system prompt:
    // - Initial test returned UTF8 characters that didn't convert properly to EBCDIC
    // - Initial test returned markdown, which is nice if you paste it into a markdown
    //   formatter but harder to read otherwise. It's a good option to request 
    //   if markdown format suits your usage.
    // The actual output produced is very dependent on both the prompt and the 
    // model selected.
    // Attempts to influence the output by writing more a more detailed prompt
    // gave worse results in all tests, so this prompt is very simple, relying
    // on the AI to figure out how to organize the information.

    private static final String SYSTEM_PROMPT = """
            You are a z/OS Systems Programmer planning installation of new maintenance.
            SMP/E holddata is provided in JSON format. Summarize the actions and information 
            in the holddata.
            Write output in ASCII safe for conversion to IBM1047 code page, not UTF8
            Format output for easy reading on a plain text terminal with maximum line length 80 characters
            """;

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .changeDefaultPropertyInclusion(incl -> incl
                    .withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    public static void main(String[] args) throws Exception
    {
        if (args.length < 3 || args.length > 4)
        {
            System.out.println("Usage: HolddataSummarize <global-csi> <target-zone> <YYYY-MM-DD> [model]");
            return;
        }

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("OPENAI_API_KEY environment variable is required.");
            return;
        }

        String csi = args[0];
        String zone = args[1];
        LocalDate since = LocalDate.parse(args[2]);
        String model = args.length == 4 ? args[3] : DEFAULT_MODEL;

        Map<String, String> fmidDescriptions = SmpeQuery.csi(csi)
                .zone(zone)
                .smodType(SysmodType.FUNCTION)
                .subEntries("DESCRIPTION")
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.entryname(),
                        entry -> entry.description() != null ? entry.description() : ""));

        var sysmods = SmpeQuery.csi(csi)
                .zone(zone)
                .installedAfter(since)
                .listSysmod()
                .stream()
                .map(entry -> entry.entryname())
                .collect(Collectors.toList());

        List<HolddataEntry> holddata = SmpeQuery.csi(csi)
                .zone(Zone.GLOBAL)
                .ename(sysmods)
                .listHolddata()
                .stream()
                .map(hold -> new HolddataEntry(
                        hold.entryname(),
                        hold.holdclass(),
                        hold.holddate(),
                        hold.holdfixcat(),
                        hold.holdfmid(),
                        fmidDescriptions.getOrDefault(hold.holdfmid(), ""),
                        hold.holdreason(),
                        hold.holdresolver(),
                        hold.holdtype(),
                        hold.comment(),
                        hold.smrtdata()))
                .toList();

        if (holddata.isEmpty())
        {
            System.out.println("No holddata found for sysmods installed after " + since + " in zone " + zone + ".");
            return;
        }

        var report = new HolddataReport(csi, zone, since, holddata);
        String json = MAPPER.writeValueAsString(report);
        requestSummary(apiKey, model, json);
    }

    private static void requestSummary(String apiKey, String model, String holddataJson) throws Exception
    {
        var requestBody = new ResponsesRequest(
                model,
                SYSTEM_PROMPT,
                "Summarize the following SMP/E holddata supplied in JSON format:\n" + holddataJson,
                false);

        String body = MAPPER.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        System.out.println("Querying OpenAI...");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        if (response.statusCode() < 200 || response.statusCode() >= 300)
        {
            System.err.println("OpenAI API error HTTP " + response.statusCode() + ":");
            System.err.println(responseBody);
            return;
        }

        JsonNode root = MAPPER.readTree(responseBody);
        String content = extractOutputText(root);
        if (content == null || content.isBlank())
        {
            System.err.println("Empty summary in OpenAI API response:");
            System.err.println(responseBody);
            return;
        }

        System.out.println(content);
    }

    private static String extractOutputText(JsonNode root)
    {
        String outputText = root.path("output_text").asString();
        if (outputText != null && !outputText.isBlank())
        {
            return outputText;
        }

        JsonNode output = root.path("output");
        if (!output.isArray())
        {
            return null;
        }

        StringBuilder text = new StringBuilder();
        for (JsonNode item : output)
        {
            if (!"message".equals(item.path("type").asString()))
            {
                continue;
            }
            JsonNode content = item.path("content");
            if (!content.isArray())
            {
                continue;
            }
            for (JsonNode part : content)
            {
                if ("output_text".equals(part.path("type").asString())
                        || "text".equals(part.path("type").asString()))
                {
                    text.append(part.path("text").asString());
                }
            }
        }

        return text.isEmpty() ? null : text.toString();
    }

    record ResponsesRequest(String model, String instructions, String input, boolean store)
    {
    }

    record HolddataEntry(
            String sysmod,
            String holdclass,
            LocalDate holddate,
            List<String> holdfixcat,
            String holdfmid,
            String fmidDescription,
            String holdreason,
            String holdresolver,
            String holdtype,
            String comment,
            Smrtdata smrtdata)
    {
    }

    record HolddataReport(
            String csi,
            String zone,
            LocalDate since,
            List<HolddataEntry> holddata)
    {
    }
}
