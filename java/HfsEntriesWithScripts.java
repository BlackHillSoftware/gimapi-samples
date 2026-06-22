import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.element.*;
import com.blackhillsoftware.gimapi.element.HfsElement;
import com.blackhillsoftware.gimapi.entry.*;

/**
 * List HFS entries that have scripts defined to be run during installation.
 */
public class HfsEntriesWithScripts
{    
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: HfsEntriesWithScripts <global-csi> <target-zone>");
            return;
        }

        String csi = args[0];
        String zone = args[1];
        
        // Get the description of each function sysmod for use later in the report
        var functions = SmpeQuery.csi(csi)
                .zone(zone)
                .smodType(SysmodType.FUNCTION)
                .subEntries("DESCRIPTION")
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.entryname(),
                        entry -> entry.description() != null ? entry.description() : ""));
        
        // Get the DDDEFs to use to build the file names                
        var dddefs = SmpeQuery.csi(csi)
            .zone(zone)
            .listDddef()
            .stream()
            .collect(Collectors.toMap(Dddef::entryname, dddef -> dddef));
        
        // Get shell script elements and map them by entry name    
        var scripts = SmpeQuery.csi(csi)
            .zone(zone)
            .listShellscr()
            .stream()
            .collect(Collectors.toMap(Shellscr::entryname, shellscr -> shellscr));        
        
        // Get elements with a shell script defined and group by fmid
        var entries = SmpeQuery.csi(csi)
            .zone(zone)
            .filter("SHSCRIPT!=''")
            .subEntries("FMID", "SYSLIB", "SHSCRIPT")         	
            .listElement()
            .stream()
            .filter(entry -> (entry instanceof HfsElement))
            .map(entry -> (HfsElement) entry)
            .collect(Collectors.groupingBy(Element::fmid));

        // Report elements, grouped by fmid
        // For each element, resolve the paths from the DDDEF
        entries.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(fmidEntry ->
            {
                System.out.format("FMID: %-8s  Description: %s%n", 
                        fmidEntry.getKey(), functions.get(fmidEntry.getKey()));
                
                fmidEntry.getValue().stream()
                    .forEach(element ->
                    {
                        System.out.format("    Element: %-23s  PATH: %s%n", 
                                element.entryname(), 
                                resolvePath(dddefs.get(element.syslib()), element.entryname()));
                            
                        var script = scripts.get(element.shscript().scriptname());
                        System.out.format("        Script: %-20s  PATH: %s%n", 
                                element.shscript(), 
                                resolvePath(dddefs.get(script.syslib()), script.entryname()));
                    });
                
            });
    }
    
    // Resolve a path by combining the DDDEF path and the
    // relative path, and normalize the result (remove ../ etc.)
    private static Path resolvePath(Dddef dddef, String path) {
        Path result = Paths.get(dddef.path(), path);
        result = result.normalize();
        return result;
    }
}
