import java.util.*;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.element.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Create JSON with information about the maintenance level of elements 
 * in the target zone (FMID, RMID, and UMID information). 
 * Optionally specify which FMIDs should be included.
 */
public class MaintenanceLevel2Json
{
    // Jackson ObjectMapper for writing JSON
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

        // build the query, add fmids if specified
        var query = SmpeQuery.csi(csi)
            .zone(zone)
            .subEntries("FMID", "RMID", "UMID");
        if (!fmids.isEmpty())
        {
            query = query.fmid(fmids);
        }

        // Run the query to get the list of elements
        // and create the ElementEntry with the information
        // to be included in JSON
        List<ElementEntry> elements = query
                .listElement()
                .stream()
                .map(element -> new ElementEntry(
                        element.entryname(),
                        element.entrytype(),
                        element.fmid(),
                        element.rmid(),
                        umid(element)))
                .toList();

        // wrap the list with information about the CSI and zone
        var report = new ReportInformation(csi, zone, elements);

        // Generate and write the json
        System.out.println(MAPPER.writeValueAsString(report));
    }

    // get umid information if this element is a type
    // that has umid, otherwise return an empty list
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

    // Allow the list of FMIDs to be separated by commas and/or spaces.
    // The argument list was already parsed by spaces, split further 
    // by commas if present.
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

    record ReportInformation(
            String csi,
            String zone,
            List<ElementEntry> elements)
    {
    }
}
