import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.element.*;
import com.blackhillsoftware.gimapi.element.HfsElement;
import com.blackhillsoftware.gimapi.entry.*;

public class HfsEntriesWithScripts
{    
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: HfsEntriesWithScripts <global-csi> <target-zone>");
            return;
        }
        
        var functions = SmpeQuery.csi(args[0])
                .zone(args[1])
                .smodType(SysmodType.FUNCTION)
                .subEntries("DESCRIPTION")
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.entryname(),
                        entry -> entry.description() != null ? entry.description() : ""));
        
        var dddefs = SmpeQuery.csi(args[0])
               .zone(args[1])
            .listDddef()
            .stream()
            .collect(Collectors.toMap(Dddef::entryname, dddef -> dddef));
        
        var scripts = SmpeQuery.csi(args[0])
                   .zone(args[1])
                .listShellscr()
                .stream()
                .collect(Collectors.toMap(Shellscr::entryname, shellscr -> shellscr));        
        
        var entries = SmpeQuery.csi(args[0])
            .zone(args[1])
            .filter("SHSCRIPT!=''")
               .subEntries("FMID", "SYSLIB", "SHSCRIPT")         	
            .listElement()
               .stream()
               .filter(entry -> (entry instanceof HfsElement))
               .map(entry -> (HfsElement) entry)
            .collect(Collectors.groupingBy(Element::fmid));

        entries.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry ->
            {
                System.out.format("FMID: %-8s  Description: %s%n", 
                        entry.getKey(), functions.get(entry.getKey()));
                
                entry.getValue().stream()
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
    
    private static Path resolvePath(Dddef dddef, String path) {
        Path result = Paths.get(dddef.path(), path);
        result = result.normalize();
        return result;
    }
}
