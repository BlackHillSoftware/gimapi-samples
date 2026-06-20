import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.SysmodType;
import com.blackhillsoftware.gimapi.entry.Sysmod;

public class MaintenanceByDate
{    
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Usage: MaintenanceByDate <global-csi> <target-zone> <yyyy-mm-dd>");
            return;
        }
        
        var date = LocalDate.parse(args[2]);
        
        var functions = SmpeQuery.csi(args[0])
                .zone(args[1])
                .smodType(SysmodType.FUNCTION)
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.entryname(),
                        entry -> entry.description() != null ? entry.description() : "",
                                (a, b) -> !a.isEmpty() ? a : b)); // duplicate, take the first non-empty
           
        var byDate = SmpeQuery.csi(args[0])
            .zone(args[1])
            .smodType(SysmodType.APAR, SysmodType.PTF)
            .installedAfter(date)
            .listSysmod()
            .stream()
            .collect(Collectors.groupingBy(Sysmod::installeddate));
        
        byDate.entrySet().stream()
            .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
            .forEach(installDate ->
            {
                System.out.format("%nDate: %s%n", installDate.getKey());
                System.out.println("================");

                var byFmid = installDate.getValue().stream()
                    .collect(Collectors.groupingBy(sysmod -> sysmod.fmid()));
                
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
