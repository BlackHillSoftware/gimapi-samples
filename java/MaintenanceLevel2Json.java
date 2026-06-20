import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.element.Element;
import com.blackhillsoftware.gimapi.element.Jar;
import com.blackhillsoftware.gimapi.element.Mac;
import com.blackhillsoftware.gimapi.element.Mod;
import com.blackhillsoftware.gimapi.element.Src;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public class MaintenanceLevel2Json
{
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .changeDefaultPropertyInclusion(incl -> incl
                    .withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    public static void main(String[] args) throws Exception
    {
        if (args.length < 2)
        {
            System.out.println("Usage: MaintenanceLevel2Json <global-csi> <target-zone> [<fmid> ...]");
            return;
        }

        String csi = args[0];
        String zone = args[1];
        List<String> fmids = parseFmids(args, 2);

        var query = SmpeQuery.csi(csi).zone(zone);
        if (!fmids.isEmpty())
        {
            query = query.fmid(fmids);
        }

        List<ElementEntry> elements = query
                .subEntries("FMID", "RMID", "UMID")
                .listElement()
                .stream()
                .sorted(Comparator.comparing(Element::entrytype)
                        .thenComparing(Element::entryname))
                .map(element -> new ElementEntry(
                        element.entryname(),
                        element.entrytype(),
                        element.fmid(),
                        element.rmid(),
                        umid(element)))
                .toList();

        var report = new MaintenanceReport(csi, zone, elements);
        System.out.println(MAPPER.writeValueAsString(report));
    }

    private static List<String> umid(Element element)
    {
        if (element instanceof Mod mod)
        {
            return mod.umid();
        }
        if (element instanceof Mac mac)
        {
            return mac.umid();
        }
        if (element instanceof Src src)
        {
            return src.umid();
        }
        if (element instanceof Jar jar)
        {
            return jar.umid();
        }
        return List.of();
    }

    private static List<String> parseFmids(String[] args, int from)
    {
        List<String> fmids = new ArrayList<>();
        for (int i = from; i < args.length; i++)
        {
            for (String part : args[i].split(","))
            {
                String fmid = part.trim();
                if (!fmid.isEmpty())
                {
                    fmids.add(fmid);
                }
            }
        }
        return fmids;
    }

    record ElementEntry(
            String entryname,
            String entrytype,
            String fmid,
            String rmid,
            List<String> umid)
    {
    }

    record MaintenanceReport(
            String csi,
            String zone,
            List<ElementEntry> elements)
    {
    }
}
