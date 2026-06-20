import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.Zone;
import com.blackhillsoftware.gimapi.subentry.Smrtdata;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public class Holddata2Json
{
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
        LocalDate since = LocalDate.parse(args[2]);

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
                        hold.holdreason(),
                        hold.holdresolver(),
                        hold.holdtype(),
                        hold.comment(),
                        hold.smrtdata()))
                .toList();

        var report = new HolddataReport(csi, zone, since, holddata);
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
