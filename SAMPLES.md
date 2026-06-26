## Samples ##

These samples are provided to demonstrate usage of the API. All samples are designed to be run as single file source code programs, without a separate compile step.

See [README.md](./README.md) for more detail on how to run the programs.

| Sample | Description |
|--------|-------------|
| [ListEntry](#listentry) | List details of any entry from the SMP/E CSI by entry name. |
| [ListFeatures](#listfeatures) | List the feature names, FMIDs and FMID descriptions known to SMP/E. |
| [ListFmid](#listfmid) | List FMIDs, installed date and description for FMIDs installed in a target zone. |
| [FmidByDescription](#fmidbydescription) | Find FMIDs with descriptions matching a search string. |
| [ListUsermods](#listusermods) | List details of usermods known to SMP/E. |
| [ListUmid](#listumid) | Find elements with UMID entries, to better understand the relationship and usage of UMID and RMID. |
| [MaintenanceByDate](#maintenancebydate) | List APARs and PTFs installed in the target zone, grouped by install date and FMID. |
| [ElementsByMaintenanceLevel](#elementsbymaintenancelevel) | List elements for an FMID grouped by effective maintenance level (RMID plus UMIDs). |
| [ResolvedHfsPaths](#resolvedhfspaths) | List HFS elements and links with the full paths resolved from DDDEFs and relative paths. |
| [HfsEntriesWithScripts](#hfsentrieswithscripts) | List HFS entries which run scripts during installation. |
| [ListHolddata](#listholddata) | Attempt to streamline reviewing HOLDDATA. |
| **SMP/E Information to JSON** | |
| [MaintenanceLevel2Json](#maintenancelevel2json) | Create JSON with information about the maintenance level of elements in the target zone. |
| [Holddata2Json](#holddata2json) | Create JSON with Holddata information for sysmods installed after a specified date. |
| [HolddataAISummary](#holddataaisummary) | Query holddata for sysmods installed after a specified date and summarize using OpenAI. |
| [InstalledSysmods2Json](#installedsysmods2json) | Create JSON with information on all sysmods installed in the target zone. Superseded sysmods show superseding sysmods and the dates they were installed. |

## Details of Samples

### ListEntry

Source: [ListEntry.java](./java/ListEntry.java)

List details of any entry from the SMP/E CSI by entry name.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar ListEntry.java MVS.GLOBAL.CSI IEFBR14
```
```
ENTRYNAME : IEFBR14
ENTRYTYPE : LMOD
  ZONENAME : MVSD
  COPIED : NO
  LASTUPD : HBB77E0
  LASTUPDTYPE : ADD
  LEPARM : RENT,NCAL
  RC : 0
  SYSLIB :
    LINKLIB
    LPALIB
  XZMODP : NO
ENTRYNAME : IEFBR14
ENTRYTYPE : LMOD
  ZONENAME : MVST
  COPIED : NO
  LASTUPD : HBB77E0
  LASTUPDTYPE : ADD
  LEPARM : RENT,NCAL
  RC : 0
  SYSLIB :
    LINKLIB
    LPALIB
  XZMODP : NO
ENTRYNAME : IEFBR14
ENTRYTYPE : MOD
  ZONENAME : MVSD
  ASSEMBLE : NO
  CSECT : IEFBR14
  DISTLIB : AOSB3
  FMID : HBB77E0
  LASTUPD : HBB77E0
  LASTUPDTYPE : ADD
  LEPARM : RENT,REFR,NCAL
  LMOD :
    IEANUC11
    IEAVEDAT
    IEFBR14
  RMID : HBB77E0
  RMIDASM : NO
  XZLMODP : NO
ENTRYNAME : IEFBR14
ENTRYTYPE : MOD
  ZONENAME : MVST
  ASSEMBLE : NO
  CSECT : IEFBR14
  DISTLIB : AOSB3
  FMID : HBB77E0
  LASTUPD : HBB77E0
  LASTUPDTYPE : ADD
  LMOD :
    IEANUC11
    IEAVEDAT
    IEFBR14
  RMID : HBB77E0
  RMIDASM : NO
  XZLMODP : NO
```
```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar ListEntry.java MVS.GLOBAL.CSI LPALIB
```
```
ENTRYNAME : LPALIB
ENTRYTYPE : DDDEF
  ZONENAME : MVST
  DATASET : SYS1.LPALIB
  INITDISP : SHR
  PROTECT : NO
  UNIT : SYSALLDA
  VOLUME : VIMVSB
  WAITFORDSN : NO
```

### ListFeatures

Source: [ListFeatures.java](./java/ListFeatures.java)

List the feature names, FMIDs and FMID descriptions known to SMP/E.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar ListFeatures.java MVS.GLOBAL.CSI
```

```
...
DZOBB310   5655-ZOS,03.01.00    z/OS V3 Base OPT. Features Installed with Base
   HCS77E0  Hardware Configuration Definition Base
   HGD3201  GDDM PGF
   HLB77C0  XL C/C++ Base
   HMOS705  Infoprint Server - IP PrintWay Base
   HNET7D0  Infoprint Server - NetSpool Base
   HOPI7D0  Infoprint Server - Print Interface Base
   HQX77E0  SDSF Base
   HRF77E0  Security Server - RACF Base
   HRM77E0  RMF
   HSM1310  DFSORT Base
   H24P111  C/C++ HOST PERFORMANCE ANALYZE
   JMQ416A  High Level Assembler Toolkit
...
IZOBB310   5655-ZOS,03.01.00    z/OS V3 Base
   EDU1H01  ICKDSF - Device Support Facilities, Base
   EER3500  Environmental Record Editing and Printing
   EMI2220  MICR/OCR
   ETI1106  TIOC
   FDU1H07  ICKDSF - Device Support Facilities, ISMF/MODS
   FDU1H08  ICKDSF - Device Support Facilities, ISMF/ENU
   HAL47C0  Not found
   HBB77E0  BCP Base
   HCM1J10  Hardware Configuration Manager
   HCPT510  Cryptographic Services - System SSL Base
   HCR77E0  Cryptographic Support - ICSF
   HCYG100  IBM z/OS Change Tracker Base
   HDZ331N  Network File System Server and Client
   HDZ3310  Data Facility System Managed Storage Base & ENU
   HFNT140  z/OS Font Collection
   HFST101  FFST
   HFX1112  PC 3270 FILE TRANSFER
   HGD3200  GDDM BASE
   HHAP90P  IBM HTTP Server
   HIF83A2  ISPF Base   
...   
```

### ListFmid

Source: [ListFmid.java](./java/ListFmid.java)

List FMIDs, installed date and description for FMIDs installed in a target zone.


```
EDU1H01    2023-06-02     ICKDSF - Device Support Facilities, Base
EER3500    2023-06-02     Environmental Record Editing and Printing
EMI2220    2023-06-02     MICR/OCR
ETI1106    2023-04-12     TIOC
FDU1H07    2023-06-02     ICKDSF - Device Support Facilities, ISMF/MODS
FDU1H08    2023-06-02     ICKDSF - Device Support Facilities, ISMF/ENU
FDU1H09    2023-06-12     ICKDSF - DEVICE SUPPORT FACILITIES, ISMF/ENU JPN
H24P111    2023-06-02     C/C++ HOST PERFORMANCE ANALYZE
HAMIK00    2024-03-14     IBM Open Enterprise SDK for Node.js
HBB77E0    2023-04-12     BCP Base
HCF773D    2023-10-13     Encryption Facility DFSMSdss Encryption
HCF7740    2023-10-13     Encryption Facility Encrypt Ser
HCM1J10    2023-06-02     Hardware Configuration Manager
HCPT510    2023-04-12     Cryptographic Services - System SSL Base
HCR77E0    2023-04-12     Cryptographic Support - ICSF
HCS77E0    2023-06-02     Hardware Configuration Definition Base
HCYG100    2023-06-02     IBM z/OS Change Tracker Base
HDZ3310    2023-04-12     Data Facility System Managed Storage Base & ENU
HDZ331N    2023-06-02     Network File System Server and Client
HFNT140    2023-06-02     z/OS Font Collection
HFNT14J    2023-06-12     z/OS Font Collection - Chinese, Japanese, Korean
HFST101    2023-06-02     FFST
HFX1112    2023-06-02     PC 3270 FILE TRANSFER
HGD3200    2023-06-02     GDDM BASE
HGD3201    2023-06-02     GDDM PGF
HHAP90P    2023-06-02     IBM HTTP Server
HHRH110    2023-10-13     IBM zCX Foundation for Red Hat OpenShift
HHZ1100    2023-10-13     IBM Container Hosting Foundation for z/OS
HIF83A2    2023-04-12     ISPF Base
HIO1106    2023-06-02     IOCP
HIP6310    2023-04-12     Communications Server IP
HJE77E0    2023-06-02     JES2 BASE
...
```

### FmidByDescription

Source: [FmidByDescription.java](./java/FmidByDescription.java)

Find FMIDs with descriptions matching a search string, across all zones.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar FmidByDescription.java MVS.GLOBAL.CSI crypt
```
```
Zone: MVSD
HCF773D installed 2023-10-13 Encryption Facility DFSMSdss Encryption
HCF7740 installed 2023-10-13 Encryption Facility Encrypt Ser
HCPT510 installed 2023-04-12 Cryptographic Services - System SSL Base
HCR77E0 installed 2023-04-12 Cryptographic Support - ICSF
HKY77E0 installed 2023-06-02 Cryptographic Services - PKI Services
JCPT51J installed 2023-06-12 Cryptographic Services - System SSL JPN

Zone: MVST
HCF773D installed 2023-10-13 Encryption Facility DFSMSdss Encryption
HCF7740 installed 2023-10-13 Encryption Facility Encrypt Ser
HCPT510 installed 2023-04-12 Cryptographic Services - System SSL Base
HCR77E0 installed 2023-04-12 Cryptographic Support - ICSF
HKY77E0 installed 2023-06-02 Cryptographic Services - PKI Services
JCPT51J installed 2023-06-12 Cryptographic Services - System SSL JPN
```

### ListUsermods

Source: [ListUsermods.java](./java/ListUsermods.java)

List details of usermods known to SMP/E.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar ListUsermods.java MVS.GLOBAL.CSI
```

```
GIM32000W    NO ENTRIES MATCHING THE SPECIFIED CRITERIA WERE FOUND.
```

### ListUmid

Source: [ListUmid.java](./java/ListUmid.java)

Find elements with UMID entries, to help understand the relationship and usage of UMID and RMID.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar ListUmid.java MVS.GLOBAL.CSI MVST
```
```
...
ENTRYNAME : HASPTRAK
ENTRYTYPE : SRC
  ZONENAME : MVST
  RMID : HJE77E0
  UMID :
    UJ92698
    UJ93156
    UJ93395
    UJ95829
    UJ96968
ENTRYNAME : HASPWARM
ENTRYTYPE : SRC
  ZONENAME : MVST
  RMID : HJE77E0
  UMID :
    UJ93156
    UJ93395
    UJ94439
    UJ96968
    UJ98569
ENTRYNAME : HASPXCF
ENTRYTYPE : SRC
  ZONENAME : MVST
  RMID : HJE77E0
  UMID :
    UJ93156
    UJ93395
    UJ96968
...
```

### MaintenanceByDate

Source: [MaintenanceByDate.java](./java/MaintenanceByDate.java)

List APARs and PTFs installed in the target zone, grouped by install date and FMID. For each date, the FMID, FMID description and list of PTFs is printed.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar MaintenanceByDate.java MVS.GLOBAL.CSI MVST 2026-05-01
```

```
Date: 2026-05-14
================

HZFS510  z/OS File System Base
   UJ99436

HJE77E0  JES2 BASE
   UJ99260    UJ99355

HRF77E0  Security Server - RACF Base
   UJ99368

HWT0500  Web Toolkit
   UJ99227

HLB77C0  XL C/C++ Base
   UO07332

JCPT511  z/OS Security Level 3 - System SSL Security Level
   UJ99449

HDZ3310  Data Facility System Managed Storage Base & ENU
   UJ98816    UJ99048    UJ99076    UJ99085    UJ99186    UJ99189    UJ99197    UJ99201
   UJ99219    UJ99241    UJ99313    UJ99318    UJ99329    UJ99341    UJ99348    UJ99359
   UJ99361    UJ99377    UJ99410    UJ99434    UJ99438    UJ99451    UJ99460    UJ99474
   UJ99506    UJ99510
```


### ElementsByMaintenanceLevel

Source: [ElementsByMaintenanceLevel.java](./java/ElementsByMaintenanceLevel.java)

List elements for an FMID grouped by effective maintenance level (RMID plus UMIDs). Maintenance levels are ordered by the latest sysmod install date. Within each level, sysmods are grouped by install date and elements are grouped by entry type.

You can find an element in the report and see the current maintenance level when it was installed, other elements updated by the same maintenance or on the same day etc.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar ElementsByMaintenanceLevel.java MVS.GLOBAL.CSI MVST HZFS510
```

```
2026-05-14   UJ99436
==========
    PROGRAM
        IOEAGFMT  IOEAGSLV  IOEDUMPF  IOEFSCM   IOEFSKN   IOEFSTHD
        IOEFSUTL  IOEZADM

2026-04-16   UJ99111
==========
    PROGRAM
        IDFDUMPF  IDFFSCM   IDFFSKN   IDFFSTHD  IDFZADM

2024-03-07   UJ94539
==========
    EXEC
        IOEE0020
    HFS
        IOEZH003
    PROGRAM
        IOEZHADD  IOEZHCK1  IOEZHMSG

2023-09-11   UJ93419
==========
    MSGENU
        IOEZHENU

Base
==========
    EXEC
        IDFDE001  IDFDE002  IOEE0019
    HFS
        IDFDH001  IDFDH002  IOEZH001  IOEZH002
    MAC
        IOEZSMFR
    PROC
        IDFDP001  IOEP0004
    SAMP
        IDFDS001  IDFDS002  IOECLNDD  IOEIZALC  IOEIZDDD  IOEIZDIR
        IOEIZMKD  IOEZS001  IOEZS002  IOEZS003  IOEZS004
```

### ResolvedHfsPaths

Source: [ResolvedHfsPaths.java](./java/ResolvedHfsPaths.java)

SMP/E processing of HDS data can be confusing. HFS element paths are relative to the path in the DDDEF, and many elements have links specified relative to that path and the link path, i.e. you end up with 2 levels of indirection.

Ths sample resolves paths for HFS elements from the DDDEFs and relative links, and reports the actul paths used for installation.

Typically you would expect paths to resolve to a path inside your service filesystem environment.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar ResolvedHfsPaths.java MVS.GLOBAL.CSI MVST
```
```
...
HZFS510  z/OS File System Base
    Entry: IDFDH001   SYSLIB: SIOEHLMD   DDDEF PATH: /sysc/usr/lpp/dfs/global/bin/IBM/
         PATH: /sysc/usr/lpp/dfs/global/bin/IBM/IDFDH001
         LINK: /sysc/usr/lpp/dfs/global/bin/IDFZADM
    Entry: IDFDH002   SYSLIB: SIOEHLMD   DDDEF PATH: /sysc/usr/lpp/dfs/global/bin/IBM/
         PATH: /sysc/usr/lpp/dfs/global/bin/IBM/IDFDH002
         LINK: /sysc/usr/lpp/dfs/global/bin/dsadm
      SYMLINK: /sysc/bin/dsadm
            -> ../usr/lpp/dfs/global/bin/dsadm
            -> /sysc/usr/lpp/dfs/global/bin/dsadm
    Entry: IOEZH001   SYSLIB: SIOEHLMD   DDDEF PATH: /sysc/usr/lpp/dfs/global/bin/IBM/
         PATH: /sysc/usr/lpp/dfs/global/bin/IBM/IOEZH001
         LINK: /sysc/usr/lpp/dfs/global/bin/IOEZADM
    Entry: IOEZH002   SYSLIB: SIOEHLMD   DDDEF PATH: /sysc/usr/lpp/dfs/global/bin/IBM/
         PATH: /sysc/usr/lpp/dfs/global/bin/IBM/IOEZH002
         LINK: /sysc/usr/lpp/dfs/global/bin/zfsadm
      SYMLINK: /sysc/bin/zfsadm
            -> ../usr/lpp/dfs/global/bin/zfsadm
            -> /sysc/usr/lpp/dfs/global/bin/zfsadm
    Entry: IOEZH003   SYSLIB: SIOEHLMD   DDDEF PATH: /sysc/usr/lpp/dfs/global/bin/IBM/
         PATH: /sysc/usr/lpp/dfs/global/bin/IBM/IOEZH003
         LINK: /sysc/usr/lpp/dfs/global/bin/ioeconv4
      SYMLINK: /sysc/usr/sbin/ioeconv4
            -> ../lpp/dfs/global/bin/ioeconv4
            -> /sysc/usr/lpp/dfs/global/bin/ioeconv4
...
```

### HfsEntriesWithScripts

Source: [HfsEntriesWithScripts.java](./java/HfsEntriesWithScripts.java)

Many HFS entries will run a script before or after installation. This sample reports the entries and the path to the script.

**Note:** this information is only available after the element has been installed. At this point the script has been run, so this information is probably not very useful. For curiosity only.

```
...
FMID: HSMA31E   Description: z/OSMF zERT Network Analyzer
    Element: IZUZNAHP                 PATH: /sysc/usr/lpp/zosmf/IBM/IZUZNAHP
        Script: IZUZNAHS,POST         PATH: /sysc/usr/lpp/zosmf/IBM/IZUZNAHS
    Element: IZUZNAPX                 PATH: /sysc/usr/lpp/zosmf/IBM/IZUZNAPX
        Script: IZUZNAPS,POST         PATH: /sysc/usr/lpp/zosmf/IBM/IZUZNAPS
FMID: HWLPEM0   Description: z/OS Liberty Embedded
    Element: BBLR2203                 PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLR2203
        Script: BBLR2203,POST         PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLR2203
    Element: BBLR2503                 PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLR2503
        Script: BBLR2503,POST         PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLR2503
    Element: BBLR2509                 PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLR2509
        Script: BBLR2509,POST         PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLR2509
    Element: BBL2503A                 PATH: /sysc/usr/lpp/liberty_zos/IBM/BBL2503A
        Script: BBLS2503,POST         PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLS2503
    Element: BBL2503B                 PATH: /sysc/usr/lpp/liberty_zos/IBM/BBL2503B
        Script: BBLS2503,POST         PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLS2503
    Element: BBL2509A                 PATH: /sysc/usr/lpp/liberty_zos/IBM/BBL2509A
        Script: BBLS2509,POST         PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLS2509
    Element: BBL2509B                 PATH: /sysc/usr/lpp/liberty_zos/IBM/BBL2509B
        Script: BBLS2509,POST         PATH: /sysc/usr/lpp/liberty_zos/IBM/BBLS2509
FMID: HXML1B0   Description: XML Toolkit for z/OS
    Element: IXMCX22B                 PATH: /sysc/usr/lpp/ixm/IBM/IXMCX22B
        Script: IXMSCRPB,POST         PATH: /sysc/usr/lpp/ixm/IBM/IXMSCRPB
    Element: IXMC580B                 PATH: /sysc/usr/lpp/ixm/IBM/IXMC580B
        Script: IXMSCRPB,POST         PATH: /sysc/usr/lpp/ixm/IBM/IXMSCRPB
...
```

### ListHolddata

Source: [ListHolddata.java](./java/ListHolddata.java)

Attempts to streamline reviewing HOLDDATA. Lists HOLDDATA from sysmods installed after a specified date.

**IPL holds** are grouped together at the start (typically, if you've seen one you've seen them all). The rest of the holds are grouped by FMID, with the FMID description included in the report.

Realistically, this probably doesn't help much. But it might be a starting point for better ideas.


## SMP/E Information to JSON - Details

### MaintenanceLevel2Json

Source: [MaintenanceLevel2Json.java](./java/MaintenanceLevel2Json.java)

Create JSON with information about the maintenance level of elements in the target zone (FMID, RMID, and UMID information). Optionally specify which FMIDs should be included.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar:/home/andrewr/java/jackson-databind-3.1.3.jar:/home/andrewr/java/jackson-core-3.1.3.jar:/home/andrewr/java/jackson-annotations-2.21.jar MaintenanceLevel2Json.java MVS.GLOBAL.CSI MVST HBB77E0
```

```
{
  "csi" : "MVS.GLOBAL.CSI",
  "zone" : "MVST",
  "elements" : [ {
    "entryname" : "IEAVSVCM",
    "entrytype" : "DATA",
    "fmid" : "HBB77E0",
    "rmid" : "UJ99293"
  }, {
    "entryname" : "IEAVSYSZ",
    "entrytype" : "DATA",
    "fmid" : "HBB77E0",
    "rmid" : "UJ97256"
  }, {
    "entryname" : "IEAVTCBM",
    "entrytype" : "DATA",
    "fmid" : "HBB77E0",
    "rmid" : "HBB77E0"
  }, {
    "entryname" : "IEAVWEBI",
    "entrytype" : "DATA",
    "fmid" : "HBB77E0",
    "rmid" : "UJ93120"
  }, ... ]
}
```

### Holddata2Json

Source: [Holddata2Json.java](./java/Holddata2Json.java)

Create JSON containing HOLDDATA entries for sysmods installed in the target zone after a specified date, for processing with another tool.

**Note:  The comment entry is not expected to be human readable. It contains the hold data comment concatenated into one string with embedded newline characters. JSON formatting escapes the newlines, resulting in one long line.**

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar:/home/andrewr/java/jackson-databind-3.1.3.jar:/home/andrewr/java/jackson-core-3.1.3.jar:/home/andrewr/java/jackson-annotations-2.21.jar Holddata2Json.java MVS.GLOBAL.CSI MVST 2026-05-01
```

```
{
  "csi" : "MVS.GLOBAL.CSI",
  "zone" : "MVST",
  "since" : "2026-05-01",
  "holddata" : [ {
    "entryname" : "UO90086",
    "holddate" : "2025-12-11",
    "holdfmid" : "EDU1H01",
    "holdreason" : "EC",
    "holdtype" : "SYSTEM",
    "comment" : "..."
  }, ... ]
}
```

### HolddataAISummary

Source: [HolddataAISummary.java](./java/HolddataAISummary.java)

Query holddata for sysmods installed in the target zone after a specified date and send the JSON to OpenAI to generate a summary of the holddata.

Requests use `store: false` so holddata and the generated summary are not retained on OpenAI servers.

Requires outbound HTTPS from z/OS to `api.openai.com` (firewall, proxy, or AT-TLS as applicable to your environment).

Set the `OPENAI_API_KEY` environment variable before running. The API key is never passed on the command line.

```
$ export OPENAI_API_KEY=sk-...
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar:/home/andrewr/java/jackson-databind-3.1.3.jar:/home/andrewr/java/jackson-core-3.1.3.jar:/home/andrewr/java/jackson-annotations-2.21.jar \
    HolddataAISummary.java MVS.GLOBAL.CSI MVST 2026-05-01
```

Optional fourth argument selects the OpenAI model (default: `gpt-5.5`):

### InstalledSysmods2Json

Source: [InstalledSysmods2Json.java](./java/InstalledSysmods2Json.java)

Create JSON information with install date for all installed sysmods in the target zone. If the sysmod has been superseded, the superseding sysmods and their install dates are included.

```
$ java -cp /home/andrewr/java/bhs-gimapi-0.9.2.jar:/home/andrewr/java/jackson-databind-3.1.3.jar:/home/andrewr/java/jackson-core-3.1.3.jar:/home/andrewr/java/jackson-annotations-2.21.jar InstalledSysmods2Json.java MVS.GLOBAL.CSI MVST
```

```
{
  "csi" : "MVS.GLOBAL.CSI",
  "zone" : "MVST",
  "sysmods" : [ {
    "entryname" : "UI95494",
    "entrytype" : "SYSMOD",
    "fmid" : "HSMA317",
    "installeddate" : "2024-03-07",
    "supby" : [ {
      "entryname" : "UI95747",
      "installeddate" : "2024-04-04"
    }, {
      "entryname" : "UI96215",
      "installeddate" : "2024-05-07"
    }, {
      "entryname" : "UO02630",
      "installeddate" : "2025-05-27"
    } ]
  }, {
    "entryname" : "UI95552",
    "entrytype" : "SYSMOD",
    "fmid" : "HSMA310",
    "installeddate" : "2024-03-07",
    "supby" : [ {
      "entryname" : "UI98720",
      "installeddate" : "2025-01-03"
    } ]
  }, {
    "entryname" : "UI95587",
    "entrytype" : "SYSMOD",
    "fmid" : "HSMA312",
    "installeddate" : "2024-03-07"
  }, ... ]
}
```
