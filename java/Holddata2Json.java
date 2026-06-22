import java.time.LocalDate;
import java.util.List;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.entry.*;
import com.blackhillsoftware.gimapi.subentry.Smrtdata;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Report hold data for sysmods installed after a specified date
 */
public class Holddata2Json
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
        if (args.length != 3)
        {
            System.out.println("Usage: Holddata2Json <global-csi> <target-zone> <YYYY-MM-DD>");
            return;
        }

        String csi = args[0];
        String zone = args[1];
        LocalDate date = LocalDate.parse(args[2]);

        // get the sysmods installed after the specified date
        // and build a list of names
        var sysmods = SmpeQuery.csi(csi)
                .zone(zone)
                .installedAfter(date)
                .listSysmod()
                .stream()
                .map(Sysmod::entryname)
                .toList();

        // Get the holddata for the list of sysmods and
        // extract the information to be included in JSON
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
                        hold.holdreason(),
                        hold.holdresolver(),
                        hold.holdtype(),
                        hold.comment(),
                        hold.smrtdata()))
                .toList();

        // Wrap it in an object recording the csi, zone, date
        var report = new HolddataReport(csi, zone, date, holddata);
        // generate and write the JSON
        System.out.println(MAPPER.writeValueAsString(report));
    }

    // records to define output

    record HolddataEntry(
            String entryname,
            String holdclass,
            LocalDate holddate,
            List<String> holdfixcat,
            String holdfmid,
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
