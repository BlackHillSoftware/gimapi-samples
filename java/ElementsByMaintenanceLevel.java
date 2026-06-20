import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.blackhillsoftware.gimapi.*;
import com.blackhillsoftware.gimapi.element.*;
import com.blackhillsoftware.gimapi.entry.*;

public class ElementsByMaintenanceLevel
{
    // Base last; otherwise newest maintenance first; tie-break on highest sysmod name.
    private static final Comparator<MaintLevel> MAINT_LEVEL_ORDER = Comparator
            .comparing(MaintLevel::isBase)
            .thenComparing(MaintLevel::latestInstallDate, Comparator.reverseOrder())
            .thenComparing(MaintLevel::maxSysmodName, Comparator.reverseOrder());

    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Usage: ElementsByMaintenanceLevel <global-csi> <target-zone> <fmid>");
            return;
        }

        String fmid = args[2];

        // get all elements maintenance info from the target zone
        var elements = SmpeQuery.csi(args[0])
            .zone(args[1])
            .fmid(fmid)
            .subEntries("FMID", "RMID", "UMID")
            .listElement();

        // Usually, maintenance level is indicated by the RMID.
        // However, some entry types can also have UMID entries
        // without changing the RMID.
        // So we will define an element maintenance level as the set of
        // RMID + UMID entries. Potentially this gives a large number of
        // entries, but in practice it should be manageable.
        // Then for each Set of RMID and UMIDs (which will frequently
        // be a single RMID value) we keep a list of
        // elements with that combination of maintenance.
        Map<Set<String>, List<Element>> elementsByMaintLevel = new HashMap<>();

        elements.forEach(element ->
        {
            Set<String> maintLevelKey = buildMaintLevelKey(element);
            elementsByMaintLevel.computeIfAbsent(maintLevelKey, x -> new ArrayList<>())
                .add(element);
        });

        // Now we want to know the dates each sysmod was installed.
        // We combine the sets from each key into a new set to de-duplicate
        // the list of sysmods.
        Set<String> allUpdates = new HashSet<>();
        elementsByMaintLevel.keySet()
            .forEach(key -> allUpdates.addAll(key));

        // Query the list of sysmods to get install dates
        var installDatesBySysmod = SmpeQuery.csi(args[0])
                .zone(args[1])
                .fmid(fmid)
                .subEntries("INSTALLDATE")
                .ename(new ArrayList<>(allUpdates))
                .listSysmod()
                .stream()
                .collect(Collectors.toMap(Sysmod::entryname, 
                    Sysmod::installeddate));

        // For each maintenance level, build report data and print with the
        // newest level first and Base last.
        elementsByMaintLevel.entrySet().stream()
                .map(e -> toMaintLevel(e.getKey(), e.getValue(), fmid, installDatesBySysmod))
                .sorted(MAINT_LEVEL_ORDER)
                .forEach(level -> printMaintLevel(level, fmid, installDatesBySysmod));
    }

    // Combine the sysmod set and element list into report fields for one level.
    private static MaintLevel toMaintLevel(Set<String> sysmodSet, List<Element> elements,
            String fmid, Map<String, LocalDate> installDatesBySysmod)
    {
        // A level containing only the FMID is reported as Base.
        boolean isBase = sysmodSet.size() == 1 && sysmodSet.contains(fmid);

        // Used to order maintenance levels newest-first in the report.
        LocalDate latestInstall = sysmodSet.stream()
                .map(installDatesBySysmod::get)
                .max(LocalDate::compareTo)
                .orElseThrow();

        // Tie-break when two levels share the same latest install date.
        String maxSysmodName = sysmodSet.stream()
                .max(String::compareTo)
                .orElseThrow();

        return new MaintLevel(isBase, latestInstall, sysmodSet, elements, maxSysmodName);
    }

    // Print sysmods grouped by install date, then elements grouped by entry type.
    private static void printMaintLevel(MaintLevel level, String fmid,
            Map<String, LocalDate> installDatesBySysmod)
    {
        if (level.isBase())
        {
            System.out.println("Base");
        }
        else
        {
            Map<LocalDate, List<String>> sysmodsByDate = level.sysmods().stream()
                    .filter(name -> !name.equals(fmid))
                    .collect(Collectors.groupingBy(installDatesBySysmod::get, HashMap::new, Collectors.toList()));

            sysmodsByDate.entrySet().stream()
                    .sorted(Map.Entry.<LocalDate, List<String>>comparingByKey().reversed())
                    .forEach(dateEntry ->
                    {
                        List<String> names = dateEntry.getValue().stream().sorted().toList();
                        String datePrefix = dateEntry.getKey() + "   ";
                        printWrappedNames(datePrefix, datePrefix.length(), names);
                    });
        }

        System.out.println("==========");

        // Elements at this maintenance level, grouped by entry type.
        level.elements().stream()
                .collect(Collectors.groupingBy(Element::entrytype))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(typeEntry ->
                {
                    System.out.println("    " + typeEntry.getKey());
                    List<String> names = typeEntry.getValue().stream()
                            .map(Element::entryname)
                            .sorted()
                            .toList();
                    printWrappedNames("        ", 8, names);
                });
        System.out.println();
    }

    private static final int ITEMS_PER_LINE = 6;
    private static final int ITEM_WIDTH = 10;

    // Print names in rows of eight; wrapped lines indent to align with the first name.
    private static void printWrappedNames(String linePrefix, int continuationIndent, List<String> names)
    {
        if (names.isEmpty())
        {
            return;
        }
        StringBuilder line = new StringBuilder(linePrefix);
        int countOnLine = 0;

        for (String name : names)
        {
            if (countOnLine >= ITEMS_PER_LINE)
            {
                System.out.println(line);
                line = new StringBuilder(" ".repeat(continuationIndent));
                countOnLine = 0;
            }
            line.append(String.format("%-" + ITEM_WIDTH + "s", name));
            countOnLine++;
        }
        System.out.println(line);
    }

    private static Set<String> buildMaintLevelKey(Element element)
    {
        Set<String> elementUpdates = new HashSet<>();
        elementUpdates.add(element.rmid());

        // Add UMID if present
        if (element instanceof Mod mod)
        {
            elementUpdates.addAll(mod.umid());
        }
        else if (element instanceof Mac mac)
        {
            elementUpdates.addAll(mac.umid());
        }
        else if (element instanceof Src src)
        {
            elementUpdates.addAll(src.umid());
        }
        else if (element instanceof Jar jar)
        {
            elementUpdates.addAll(jar.umid());
        }
        return elementUpdates;
    }

    private record MaintLevel(
            boolean isBase,
            LocalDate latestInstallDate,
            Set<String> sysmods,
            List<Element> elements,
            String maxSysmodName)
    {
    }
}
