import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.entry.*;

/**
 * Find FMIDs by description across all zones, with status information.
 */
public class FmidByDescription
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: FmidByDescription <global-csi> <searchfor>");
            return;
        }

        String csi = args[0];
        String searchFor = args[1];

        // We will use a regex for the search, translating wildcards to 
        // regex format. Since we're using it anyway, also allow the user
        // to specify their own regex by surrounding it with slashes i.e. /.../
        Pattern pattern;
        try
        {
            pattern = compilePattern(searchFor);
        }
        catch (PatternSyntaxException e)
        {
            System.out.println("Invalid regular expression: " + e.getMessage());
            return;
        }

        // query function sysmods, with status and description and group by zone
        var byZone = SmpeQuery.csi(csi)
            .zone(Zone.ALL)
            .smodType(SysmodType.FUNCTION)
            .subEntries("DESCRIPTION", "DELBY", "LASTSUP", "INSTALLDATE", "RECDATE")
            .listSysmod()
            .stream()
            .filter(s -> s.description() != null && !s.description().isEmpty())
            .filter(s -> pattern.matcher(s.description()).find())
            .collect(Collectors.groupingBy(Sysmod::zonename));

        // sort the zone entries, GLOBAL first then alphabetically
        var zones = byZone.entrySet().stream()
            .sorted((a, b) -> globalFirst(a.getKey(), b.getKey()))
            .toList();

        // print fmids from each zone
        for (int i = 0; i < zones.size(); i++)
        {
            if (i > 0)
            {
                System.out.println();
            }
            var zoneEntry = zones.get(i);
            System.out.format("Zone: %s%n", zoneEntry.getKey());
            zoneEntry.getValue().stream()
                .sorted(Comparator.comparing(Sysmod::entryname))
                .forEachOrdered(s ->
                {
                    String[] status = statusColumns(s);
                    System.out.format("%-7s %-9s %-10s %s%n",
                        s.entryname(),
                        status[0],
                        status[1],
                        s.description());
                });
        }
    }

    // GLOBAL first, then alphabetical
    private static int globalFirst(String a, String b)
    {
        if (a.equals(b)) return 0;
        if ("GLOBAL".equals(a)) return -1;
        if ("GLOBAL".equals(b)) return 1;
        return a.compareTo(b);
    }

    // Set up the regex
    private static Pattern compilePattern(String searchFor)
    {
        // if searchFor starts and ends with "/" the user has specified their own regex
        if (searchFor.length() >= 2 && searchFor.startsWith("/") && searchFor.endsWith("/"))
        {
            return Pattern.compile(searchFor.substring(1, searchFor.length() - 1),
                Pattern.CASE_INSENSITIVE);
        }

        // otherwise translate "*"" to ".*" and escape any other regex characters
        String regex = Arrays.stream(searchFor.split("\\*", -1))
            .map(Pattern::quote)
            .collect(Collectors.joining(".*"));
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    // return status information for the sysmod
    private static String[] statusColumns(Sysmod s)
    {
        if (s.delby() != null)
        {
            return new String[] { "delby", s.delby() };
        }
        if (s.lastsup() != null)
        {
            return new String[] { "supby", s.lastsup() };
        }
        if (s.installeddate() != null)
        {
            return new String[] { "installed", s.installeddate().toString() };
        }
        if (s.receiveddate() != null)
        {
            return new String[] { "received", s.receiveddate().toString() };
        }
        return new String[] { "", "" };
    }
}
