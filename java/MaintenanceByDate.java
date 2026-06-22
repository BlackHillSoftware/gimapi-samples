import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.entry.Sysmod;

/**
 * List maintenance since a specified date, grouped by install date
 */
public class MaintenanceByDate
{    
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Usage: MaintenanceByDate <global-csi> <target-zone> <yyyy-mm-dd>");
            return;
        }

        String csi = args[0];
        String zone = args[1];
        LocalDate date = LocalDate.parse(args[2]);
        
        // Get FMID descriptions for use in the report
        var functions = SmpeQuery.csi(csi)
                .zone(zone)
                .smodType(SysmodType.FUNCTION)
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.entryname(),
                        entry -> entry.description() != null ? entry.description() : "",
                                (a, b) -> !a.isEmpty() ? a : b)); // duplicate, take the first non-empty
        
        // List APARs and PTFs installed since the specified date,
        // and group by installed date
        var byDate = SmpeQuery.csi(csi)
            .zone(zone)
            .smodType(SysmodType.APAR, SysmodType.PTF)
            .installedAfter(date)
            .listSysmod()
            .stream()
            .collect(Collectors.groupingBy(Sysmod::installeddate));
        
        // sort and print by install date, most recent first
        byDate.entrySet().stream()
            .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
            .forEach(installDate ->
            {
                System.out.format("%nDate: %s%n", installDate.getKey());
                System.out.println("================");

                // group installed sysmods by fmid
                var byFmid = installDate.getValue().stream()
                    .collect(Collectors.groupingBy(sysmod -> sysmod.fmid()));
                
                // print the fmid and description, followed by the list of sysmods
                byFmid.entrySet().stream()
                    .forEach(fmidEntry ->
                    {
                        System.out.format("%n%-8s %s%n", 
                            fmidEntry.getKey(), 
                            functions.getOrDefault(fmidEntry.getKey(), ""));
                        List<Sysmod> fmidSysmods = fmidEntry.getValue();
                        for (int i=0; i < fmidSysmods.size(); i++)
                        {
                            System.out.format("   %-8s", fmidSysmods.get(i).entryname());
                            if ((i + 1) % 8 == 0 || i == fmidSysmods.size() - 1)
                            {
                                System.out.println();
                            }
                        }
                    });	
            });
    }
}
