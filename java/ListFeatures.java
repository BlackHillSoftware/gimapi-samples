import java.util.Comparator;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.SmpeQuery;
import com.blackhillsoftware.gimapi.SysmodType;
import com.blackhillsoftware.gimapi.Zone;
import com.blackhillsoftware.gimapi.entry.*;

public class ListFeatures
{    
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: ListFeatures <global-csi>");
            return;
        }
        // get all features from the global zone
        var features = SmpeQuery.csi(args[0])
            .zone(Zone.GLOBAL)
            .listFeature();
        
        // List all function sysmods, and create a Map
        // from name to function description.
        // For some reason we don't seem to get a description
        // from the global zone sysmod
        var descriptions = SmpeQuery.csi(args[0])
                .zone(Zone.ALL)
                .smodType(SysmodType.FUNCTION)
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.entryname(),
                        entry -> entry.description() != null ? entry.description() : "",
                        (a, b) -> !a.isEmpty() ? a : b)); // duplicate, take the first non-empty
        
        // List results: features with descriptions and fmids with descriptions
        features.stream()
            .sorted(Comparator.comparing(Feature::entryname))
            .forEachOrdered(feature -> 
            {
                System.out.format("%n%-10s %-20s %s%n", feature.entryname(), feature.product(), feature.description());
                feature.fmid().forEach(fmid -> {
                    System.out.format("   %-7s  %s%n", fmid, descriptions.getOrDefault(fmid, "Not found"));
                });
            });      
    }
}
