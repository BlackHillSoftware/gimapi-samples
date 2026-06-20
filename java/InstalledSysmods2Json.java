import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.entry.Sysmod;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public class InstalledSysmods2Json
{
    // Set up Jackson Json conversion
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .changeDefaultPropertyInclusion(incl -> incl
                    .withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .build();

    public static void main(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println("Usage: InstalledSysmods2Json <global-csi> <target-zone>");
            return;
        }

        String csi = args[0];
        String zone = args[1];

        List<Sysmod> allSysmods = SmpeQuery.csi(csi)
                .zone(zone)
                .subEntries("FMID", "INSTALLDATE", "LASTSUP", "SUPBY")
                .listSysmod()
                .stream()
                .filter(s -> s.installeddate() != null)
                .collect(Collectors.toList());

        // Map installed dates by sysmod name, which we will use to 
        // look up install dates for superseding sysmods.
        Map<String, LocalDate> installDatesByName = allSysmods.stream()
                .collect(Collectors.toMap(Sysmod::entryname, Sysmod::installeddate));

        // build an entry for each sysmod, which will include the install date,
        // any superseding sysmods and the date they were installed.
        List<InstalledSysmodEntry> sysmods = allSysmods.stream()
                .map(sysmod -> new InstalledSysmodEntry(
                        sysmod.entryname(),
                        sysmod.entrytype(),
                        sysmod.fmid(),
                        sysmod.installeddate(),
                        supbyEntries(sysmod, installDatesByName)))
                .toList();

        // wrap it in an object with csi and zone information
        var report = new InstalledSysmods(csi, zone, sysmods);

        System.out.println(MAPPER.writeValueAsString(report));
    }

    // build the list of superseding sysmods
    private static List<SupbySysmod> supbyEntries(Sysmod sysmod, Map<String, LocalDate> installDatesByName)
    {
        if (sysmod.lastsup() == null) return Collections.emptyList();
        List<SupbySysmod> supby = new ArrayList<>();
        for (String id : sysmod.supby())
        {
            supby.add(new SupbySysmod(id, installDatesByName.get(id)));
        }
        supby.add(new SupbySysmod(sysmod.lastsup(), installDatesByName.get(sysmod.lastsup())));
        return supby;
    }

    // record structures to organize the information

    record SupbySysmod(String entryname, LocalDate installeddate) {}

    record InstalledSysmodEntry(
            String entryname,
            String entrytype,
            String fmid,
            LocalDate installeddate,
            List<SupbySysmod> supby)
    {
    }

    record InstalledSysmods(
            String csi,
            String zone,
            List<InstalledSysmodEntry> sysmods)
    {
    }
}
