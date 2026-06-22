import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.element.*;
import com.blackhillsoftware.gimapi.entry.*;

/**
 * List HFS elements, with the paths resolved from DDDEFs and relative paths
 */
public class ResolvedHfsPaths
{    
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: ResolvedHfsPaths <global-csi> <target-zone>");
            return;
        }

        String csi = args[0];
        String zone = args[1];
        
        // Get fmid descriptions for use later in the report
        var functions = SmpeQuery.csi(csi)
            .zone(zone)
            .smodType(SysmodType.FUNCTION)
            .subEntries("DESCRIPTION")
            .listSysmod()
            .stream()
            .collect(Collectors.toMap(
                    entry -> entry.entryname(),
                    entry -> entry.description() != null ? entry.description() : ""));
        
        // Get DDDEFs and map by name
        var dddefs = SmpeQuery.csi(csi)
            .zone(zone)
            .listDddef()
            .stream()
            .collect(Collectors.toMap(Dddef::entryname, dddef -> dddef));
        
        // Get all elements, filter to HFS elements and map by fmid
        var hfsByFmid = SmpeQuery.csi(csi)
                .zone(zone)
                // limit data returned because we are about to get all elements
                .subEntries("FMID", "SYSLIB", "LINK","SYMPATH", "SYMLINK") 
                .listElement()
                .stream()
                .filter(entry -> (entry instanceof HfsElement))
                .map(entry -> (HfsElement) entry)
                .collect(Collectors.groupingBy(entry -> entry.fmid()));
           
        // sort and report by fmid
        hfsByFmid.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(fmidEntry ->
            {
                System.out.format("%n%-8s %s%n", fmidEntry.getKey(), functions.get(fmidEntry.getKey()));
                
                // report each entry
                fmidEntry.getValue().stream()
                    .sorted(Comparator.comparing(HfsElement::entryname))
                    .forEachOrdered(entry -> 
                    {
                        // entry information
                        var dddef = dddefs.get(entry.syslib());
                        System.out.format("    Entry: %-8s   SYSLIB: %-8s   DDDEF PATH: %s%n", 
                                entry.entryname(), 
                                entry.syslib(),
                                dddef.path());

                        // resolve and report element path and links
                        System.out.format("         PATH: %s%n", 
                                resolvePath(dddef.path(), entry.entryname()));

                        entry.link().forEach(link -> 
                        {
                            System.out.format("         LINK: %s%n", 
                                    resolvePath(dddef.path(), link));
                        });
                    
                        // report symbolic links,
                        // first we resolve the path to the symbolic link,
                        // then report the symbolic link target relative to the 
                        // resolved symbolic link
                        for (int i=0; i < entry.symlink().size(); i++)
                        {
                            Path symlink = resolvePath(dddef.path(), entry.symlink().get(i));
                            System.out.format("      SYMLINK: %s%n", symlink);

                            String sympath = i < entry.sympath().size() ? 
                                    entry.sympath().get(i) 
                                    : entry.sympath().get(entry.sympath().size() - 1);

                            System.out.format("            -> %s%n", sympath);

                            // The sympath needs to be relative to the 
                            // symlink directory. symlink was created using the 
                            // DDDEF, so it should always have a parent. 
                            // (We don't expect DDDEFs installing into the root directory.)
                            // sympath could theoretically also be an absolute 
                            // path starting with "/" in which case it's not 
                            // relative to the symbolic link
                            System.out.format("            -> %s%n",
                                    sympath.startsWith("/") ?
                                        sympath 
                                        : resolvePath(symlink.getParent().toString(), sympath));
                        }
                    });
        });
    }

    // combine 2 paths, and normalize them (remove redundant elements 
    // like ../ by walking up the directory tree)
    private static Path resolvePath(String path1, String path2) {
        Path result = Paths.get(path1, path2);
        result = result.normalize();
        return result;
    }
}
