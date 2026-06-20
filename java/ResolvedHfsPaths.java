import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.element.*;
import com.blackhillsoftware.gimapi.entry.*;

public class ResolvedHfsPaths
{    
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: ResolvedHfsPaths <global-csi> <target-zone>");
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
        
        var hfsByFmid = SmpeQuery.csi(args[0])
                   .zone(args[1])
                   // limit data returned because we are about to get all elements
                   .subEntries("FMID", "SYSLIB", "LINK","SYMPATH", "SYMLINK") 
                   .listElement()
                   .stream()
                   .filter(entry -> (entry instanceof HfsElement))
                   .map(entry -> (HfsElement) entry)
                .collect(Collectors.groupingBy(entry -> entry.fmid()));
                          
        hfsByFmid.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(fmidEntry ->
            {
                System.out.format("%n%-8s %s%n", fmidEntry.getKey(), functions.get(fmidEntry.getKey()));
                        
                fmidEntry.getValue().stream()
                    .sorted(Comparator.comparing(HfsElement::entryname))
                    .forEachOrdered(entry -> 
                    {
                        var dddef = dddefs.get(entry.syslib());
                        System.out.format("    Entry: %-8s   SYSLIB: %-8s   DDDEF PATH: %s%n", 
                                entry.entryname(), 
                                entry.syslib(),
                                dddef.path());

                        System.out.format("         PATH: %s%n", 
                                resolvePath(dddef.path(), entry.entryname()));

                        entry.link().forEach(link -> 
                        {
                            System.out.format("         LINK: %s%n", 
                                    resolvePath(dddef.path(), link));
                        });
                    
                        for (int i=0; i < entry.symlink().size(); i++)
                        {
                            Path symlink = resolvePath(dddef.path(), entry.symlink().get(i));
                            System.out.format("      SYMLINK: %s%n", symlink);

                            String sympath = i < entry.sympath().size() ? 
                                    entry.sympath().get(i) 
                                    : entry.sympath().get(entry.sympath().size() - 1);

                            System.out.format("            -> %s%n", sympath);

                            // The relative path needs to be relative to the 
                            // symlink directory. symlink was created using the 
                            // DDDEF, so it should always have a parent
                            System.out.format("            -> %s%n", 
                                    resolvePath(symlink.getParent().toString(), sympath));
                        }
                    });
        });
    }

    private static Path resolvePath(String path1, String path2) {
        Path result = Paths.get(path1, path2);
        result = result.normalize();
        return result;
    }
}
