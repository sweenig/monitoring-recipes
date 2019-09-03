/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/
//single term examples
Oid = "1.3.6.1.2.1.2.2.1" //polling the interfaces table (returns "metricOID.wildvalue = datavalue")
//Oid = "1.3.6.1.2.1.2.2.1.2" //polling the interfaces table for one column (returns "wildvalue = datavalue")
//Oid = "1.3.6.1.2.1" //polling an entire tree
wildvalueTerms = 1

//multiple term examples
//Oid = "1.3.6.1.4.1.34086.2.2.17.3.1.1"
//wildvalueTerms = 2

import com.santaba.agent.groovyapi.snmp.Snmp

def host = hostProps.get('system.hostname')
def props = hostProps.toProperties()
int timeout = 10000 // 10 sec timeout.

def snmpMapToTable(snmpmap, int wildvalueTerms = 1) {
    //println("Raw result of snmpwalk of ${Oid}:\n${snmpmap}\n${"=".multiply(80)}")
    rows = [:]
    snmpmap.each {k,v ->
        splits = k.tokenize(".")
        col = splits.dropRight(wildvalueTerms).join(".")
        wildvalue = splits.takeRight(wildvalueTerms).join(".")
        if (!(rows.containsKey(wildvalue))) { rows[wildvalue] = [:] }
        rows[wildvalue][col] = v
    }
    return rows
}

def sortSnmpWalkTable(table, int wildvalueTerms = 1) {
    if (wildvalueTerms > 2) {
        println("More than 2 terms in the wildvalue are not supported.");
        return [];
    }
    if (wildvalueTerms == 1) {
        rowsWildvalueSorted = table.keySet().collect { it.toInteger() }.sort()
    } else {
        rowsWildvalueSorted = table.keySet().collect { Float.parseFloat(it)}.sort()
    }
    return rowsWildvalueSorted
}

def sortDataKeys(data){
    if (data.keySet().size() > 1){ //if there is more than one data point per wildvalue
        if (data.keySet()[0].tokenize(".").size() > 1){return data.keySet()} //if there is more than one term in the data key
        else {
            println("Data sorted by column ID")
            return data.keySet().collect { it.toInteger() }.sort().collect{it.toString()}
        }
    } else {return data.keySet()}
}

def pprintSnmpWalkTable(table) {
    if (table.size() == 0) {
        println("No data returned.")
    } else {
        sortSnmpWalkTable(table).each { k ->
            println("Wildvalue: ${k.toString()}:")
            data = table[k.toString()]
            sortDataKeys(data).each {println("  ${it ?: Oid.tokenize(".")[-1]}.${"##"+"WILDVALUE"+"##"}: ${data[it]}")}
        }
    }
}

def flattenSnmpWalkTable(table) {
    flat = []
    sortSnmpWalkTable(table).each { k ->
        data = table[k.toString()].each {key, val ->
            flat << [k,key, val]
        }
    }
    return flat
}

walkResult = Snmp.walkAsMap(host, Oid, props, timeout)
entryRaw = snmpMapToTable(walkResult, wildvalueTerms)
println(walkResult)
println(entryRaw)
pprintSnmpWalkTable(entryRaw)
flattenSnmpWalkTable(entryRaw).each{println(it)}
return 0

/*******************************************************************************
* Sample output against my machine
********************************************************************************
[8.6:2, 4.10:1500, 8.7:1, 8.8:1, 4.12:1500, 4.14:1500, 4.16:1500, 3.44:6, 22.5:0.0, 22.4:0.0, 22.3:0.0, 22.2:0.0, 22.1:0.0, 22.8:0.0, 22.7:0.0, 22.6:0.0, 17.16:163684, 16.44:6623314, 17.12:8614157, 18.8:0, 17.14:8725486, 18.7:0, 17.10:20997, 10.6:0, 10.5:0, 10.4:0, 10.3:0, 10.2:3944686696, 10.1:8962002, 22.44:0.0, 18.6:0, 18.5:0, 7.1:1, 18.4:0, 7.2:1, 18.3:0, 7.3:1, 18.2:0, 7.4:1, 18.1:0, 7.5:1, 10.8:1684285, 7.6:2, 10.7:4161096832, 7.7:1, 7.8:1, 15.44:0, 16.12:2109869568, 16.10:4061394, 16.16:16420222, 16.14:2355836987, 22.14:0.0, 22.16:0.0, 9.44:2 days, 14:54:42.04, 21.6:0, 21.5:0, 21.4:0, 21.3:0, 21.2:0, 21.1:0, 22.10:0.0, 22.12:0.0, 21.44:0, 21.8:0, 21.7:0, 5.10:4294967295, 4.44:1500, 5.12:4294967295, 5.14:4294967295, 11.10:0, 5.16:4294967295, 10.44:1620014, 11.12:12459122, 17.8:30735, 11.14:12643925, 17.7:17462122, 6.1:, 17.6:0, 6.2:90:e6:ba:59:1b:18, 11.16:135696, 17.5:0, 6.3:00:50:56:c0:00:01, 17.4:20150, 6.4:00:50:56:c0:00:08, 17.3:20152, 6.5:52:54:00:9b:4b:e0, 17.2:37110637, 6.6:52:54:00:9b:4b:e0, 17.1:44266, 6.7:02:42:39:1b:cc:e3, 6.8:02:42:55:44:3f:06, 10.10:0, 20.7:0, 20.6:0, 20.5:0, 20.4:0, 20.3:0, 20.2:0, 20.1:0, 10.12:20649171, 10.14:121066690, 10.16:77756077, 20.8:0, 5.1:10000000, 16.8:17029956, 5.2:100000000, 16.7:179090566, 5.3:0, 16.6:0, 5.4:0, 16.5:0, 5.5:0, 16.4:0, 5.6:10000000, 16.3:0, 5.7:0, 16.2:979342493, 5.8:0, 16.1:8962002, 6.12:4e:c1:4e:e6:07:90, 5.44:4294967295, 6.14:0a:2c:4f:3a:eb:5c, 6.16:12:db:e0:82:ca:a2, 19.44:0, 6.10:06:31:50:31:a5:fb, 15.12:0, 14.44:0, 15.10:0, 21.16:0, 15.16:0, 21.14:0, 15.14:0, 20.44:0, 21.12:0, 1.12:12, 1.10:10, 15.1:0, 4.1:65536, 4.2:1500, 4.3:1500, 15.8:0, 21.10:0, 4.4:1500, 15.7:0, 4.5:1500, 15.6:0, 4.6:1500, 15.5:0, 4.7:1500, 15.4:0, 4.8:1500, 15.3:0, 15.2:0, 14.10:0, 20.16:0, 14.14:0, 20.14:0, 13.44:0, 14.12:0, 20.12:0, 1.16:16, 1.14:14, 20.10:0, 14.16:0, 6.44:62:d0:3f:14:ef:85, 7.12:1, 7.14:1, 7.16:1, 7.10:1, 14.2:0, 14.1:0, 3.1:24, 3.2:6, 3.3:6, 3.4:6, 3.5:6, 14.8:0, 3.6:6, 14.7:0, 3.7:6, 14.6:0, 3.8:6, 14.5:0, 14.4:0, 14.3:0, 2.14:veth9a64fa1, 2.12:veth2bc8fbd, 1.44:44, 2.10:veth92ca0b0, 19.14:0, 19.16:0, 19.10:0, 19.12:0, 18.44:0, 13.3:0, 13.2:0, 13.1:0, 2.1:lo, 2.2:NVIDIA Corporation MCP77 Ethernet, 2.16:veth497df7f, 2.3:vmnet1, 2.4:vmnet8, 2.5:virbr0, 2.6:virbr0-nic, 2.7:br-6a2604a91ac1, 13.8:0, 2.8:docker0, 13.7:0, 13.6:0, 13.5:0, 13.4:0, 18.16:0, 8.16:1, 8.14:1, 17.44:18778, 18.12:0, 18.14:0, 18.10:0, 8.12:1, 7.44:1, 8.10:1, 13.10:0, 12.44:0, 13.14:0, 13.12:0, 3.14:6, 2.44:vethd608288, 3.12:6, 3.10:6, 12.4:0, 12.3:0, 12.2:2662, 12.1:0, 1.1:1, 1.2:2, 1.3:3, 1.4:4, 1.5:5, 1.6:6, 1.7:7, 1.8:8, 13.16:0, 9.1:0:00:00.00, 12.8:0, 9.2:0:00:00.00, 12.7:0, 9.3:0:00:06.31, 12.6:0, 9.4:0:00:06.31, 12.5:0, 9.5:0:00:09.32, 9.6:0:00:12.32, 9.7:3:03:43.67, 9.8:0:00:18.32, 12.10:0, 11.44:4506, 12.12:0, 3.16:6, 12.14:0, 12.16:0, 9.16:3:03:43.67, 9.14:3:03:43.67, 19.8:0, 19.7:0, 19.6:0, 8.44:1, 9.12:3:03:43.67, 9.10:0:00:18.32, 11.5:0, 11.4:0, 11.3:0, 11.2:28619544, 11.1:44266, 19.5:0, 19.4:0, 19.3:0, 8.1:1, 19.2:0, 8.2:1, 19.1:0, 8.3:1, 11.8:6882, 8.4:1, 11.7:25238743, 8.5:2, 11.6:0]
[6:[8:2, 22:0.0, 10:0, 18:0, 7:2, 21:0, 17:0, 6:52:54:00:9b:4b:e0, 20:0, 16:0, 5:10000000, 15:0, 4:1500, 3:6, 14:0, 2:virbr0-nic, 13:0, 1:6, 12:0, 9:0:00:12.32, 19:0, 11:0], 10:[4:1500, 17:20997, 16:4061394, 22:0.0, 5:4294967295, 11:0, 10:0, 6:06:31:50:31:a5:fb, 15:0, 1:10, 21:0, 14:0, 20:0, 7:1, 2:veth92ca0b0, 19:0, 18:0, 8:1, 13:0, 3:6, 12:0, 9:0:00:18.32], 7:[8:1, 22:0.0, 18:0, 10:4161096832, 7:1, 21:0, 17:17462122, 6:02:42:39:1b:cc:e3, 20:0, 16:179090566, 5:0, 15:0, 4:1500, 14:0, 3:6, 2:br-6a2604a91ac1, 13:0, 1:7, 12:0, 9:3:03:43.67, 19:0, 11:25238743], 8:[8:1, 22:0.0, 18:0, 10:1684285, 7:1, 21:0, 17:30735, 6:02:42:55:44:3f:06, 20:0, 16:17029956, 5:0, 15:0, 4:1500, 14:0, 3:6, 13:0, 2:docker0, 1:8, 12:0, 9:0:00:18.32, 19:0, 11:6882], 12:[4:1500, 17:8614157, 16:2109869568, 22:0.0, 5:4294967295, 11:12459122, 10:20649171, 6:4e:c1:4e:e6:07:90, 15:0, 21:0, 1:12, 14:0, 20:0, 7:1, 2:veth2bc8fbd, 19:0, 18:0, 8:1, 13:0, 3:6, 12:0, 9:3:03:43.67], 14:[4:1500, 17:8725486, 16:2355836987, 22:0.0, 5:4294967295, 11:12643925, 10:121066690, 6:0a:2c:4f:3a:eb:5c, 21:0, 15:0, 14:0, 20:0, 1:14, 7:1, 2:veth9a64fa1, 19:0, 8:1, 18:0, 13:0, 3:6, 12:0, 9:3:03:43.67], 16:[4:1500, 17:163684, 16:16420222, 22:0.0, 5:4294967295, 11:135696, 10:77756077, 6:12:db:e0:82:ca:a2, 21:0, 15:0, 20:0, 1:16, 14:0, 7:1, 19:0, 2:veth497df7f, 18:0, 8:1, 13:0, 3:6, 12:0, 9:3:03:43.67], 44:[3:6, 16:6623314, 22:0.0, 15:0, 9:2 days, 14:54:42.04, 21:0, 4:1500, 10:1620014, 5:4294967295, 19:0, 14:0, 20:0, 13:0, 6:62:d0:3f:14:ef:85, 1:44, 18:0, 17:18778, 7:1, 12:0, 2:vethd608288, 11:4506, 8:1], 5:[22:0.0, 10:0, 18:0, 7:1, 21:0, 17:0, 6:52:54:00:9b:4b:e0, 20:0, 16:0, 5:0, 4:1500, 15:0, 3:6, 14:0, 2:virbr0, 13:0, 1:5, 12:0, 9:0:00:09.32, 11:0, 19:0, 8:2], 4:[22:0.0, 10:0, 18:0, 7:1, 21:0, 17:20150, 6:00:50:56:c0:00:08, 20:0, 5:0, 16:0, 4:1500, 15:0, 3:6, 14:0, 2:vmnet8, 13:0, 12:0, 1:4, 9:0:00:06.31, 11:0, 19:0, 8:1], 3:[22:0.0, 10:0, 18:0, 7:1, 21:0, 6:00:50:56:c0:00:01, 17:20152, 20:0, 5:0, 16:0, 4:1500, 15:0, 3:6, 14:0, 13:0, 2:vmnet1, 12:0, 1:3, 9:0:00:06.31, 11:0, 19:0, 8:1], 2:[22:0.0, 10:3944686696, 7:1, 18:0, 21:0, 6:90:e6:ba:59:1b:18, 17:37110637, 20:0, 5:100000000, 16:979342493, 4:1500, 15:0, 14:0, 3:6, 13:0, 2:NVIDIA Corporation MCP77 Ethernet, 12:2662, 1:2, 9:0:00:00.00, 11:28619544, 19:0, 8:1], 1:[22:0.0, 10:8962002, 7:1, 18:0, 21:0, 6:, 17:44266, 20:0, 5:10000000, 16:8962002, 15:0, 4:65536, 14:0, 3:24, 13:0, 2:lo, 12:0, 1:1, 9:0:00:00.00, 11:44266, 8:1, 19:0]]
Wildvalue: 1:
Data sorted by column ID
  1.##WILDVALUE##: 1
  2.##WILDVALUE##: lo
  3.##WILDVALUE##: 24
  4.##WILDVALUE##: 65536
  5.##WILDVALUE##: 10000000
  6.##WILDVALUE##:
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 0:00:00.00
  10.##WILDVALUE##: 8962002
  11.##WILDVALUE##: 44266
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 8962002
  17.##WILDVALUE##: 44266
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 2:
Data sorted by column ID
  1.##WILDVALUE##: 2
  2.##WILDVALUE##: NVIDIA Corporation MCP77 Ethernet
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 100000000
  6.##WILDVALUE##: 90:e6:ba:59:1b:18
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 0:00:00.00
  10.##WILDVALUE##: 3944686696
  11.##WILDVALUE##: 28619544
  12.##WILDVALUE##: 2662
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 979342493
  17.##WILDVALUE##: 37110637
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 3:
Data sorted by column ID
  1.##WILDVALUE##: 3
  2.##WILDVALUE##: vmnet1
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 0
  6.##WILDVALUE##: 00:50:56:c0:00:01
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 0:00:06.31
  10.##WILDVALUE##: 0
  11.##WILDVALUE##: 0
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 0
  17.##WILDVALUE##: 20152
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 4:
Data sorted by column ID
  1.##WILDVALUE##: 4
  2.##WILDVALUE##: vmnet8
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 0
  6.##WILDVALUE##: 00:50:56:c0:00:08
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 0:00:06.31
  10.##WILDVALUE##: 0
  11.##WILDVALUE##: 0
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 0
  17.##WILDVALUE##: 20150
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 5:
Data sorted by column ID
  1.##WILDVALUE##: 5
  2.##WILDVALUE##: virbr0
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 0
  6.##WILDVALUE##: 52:54:00:9b:4b:e0
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 2
  9.##WILDVALUE##: 0:00:09.32
  10.##WILDVALUE##: 0
  11.##WILDVALUE##: 0
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 0
  17.##WILDVALUE##: 0
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 6:
Data sorted by column ID
  1.##WILDVALUE##: 6
  2.##WILDVALUE##: virbr0-nic
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 10000000
  6.##WILDVALUE##: 52:54:00:9b:4b:e0
  7.##WILDVALUE##: 2
  8.##WILDVALUE##: 2
  9.##WILDVALUE##: 0:00:12.32
  10.##WILDVALUE##: 0
  11.##WILDVALUE##: 0
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 0
  17.##WILDVALUE##: 0
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 7:
Data sorted by column ID
  1.##WILDVALUE##: 7
  2.##WILDVALUE##: br-6a2604a91ac1
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 0
  6.##WILDVALUE##: 02:42:39:1b:cc:e3
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 3:03:43.67
  10.##WILDVALUE##: 4161096832
  11.##WILDVALUE##: 25238743
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 179090566
  17.##WILDVALUE##: 17462122
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 8:
Data sorted by column ID
  1.##WILDVALUE##: 8
  2.##WILDVALUE##: docker0
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 0
  6.##WILDVALUE##: 02:42:55:44:3f:06
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 0:00:18.32
  10.##WILDVALUE##: 1684285
  11.##WILDVALUE##: 6882
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 17029956
  17.##WILDVALUE##: 30735
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 10:
Data sorted by column ID
  1.##WILDVALUE##: 10
  2.##WILDVALUE##: veth92ca0b0
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 4294967295
  6.##WILDVALUE##: 06:31:50:31:a5:fb
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 0:00:18.32
  10.##WILDVALUE##: 0
  11.##WILDVALUE##: 0
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 4061394
  17.##WILDVALUE##: 20997
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 12:
Data sorted by column ID
  1.##WILDVALUE##: 12
  2.##WILDVALUE##: veth2bc8fbd
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 4294967295
  6.##WILDVALUE##: 4e:c1:4e:e6:07:90
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 3:03:43.67
  10.##WILDVALUE##: 20649171
  11.##WILDVALUE##: 12459122
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 2109869568
  17.##WILDVALUE##: 8614157
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 14:
Data sorted by column ID
  1.##WILDVALUE##: 14
  2.##WILDVALUE##: veth9a64fa1
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 4294967295
  6.##WILDVALUE##: 0a:2c:4f:3a:eb:5c
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 3:03:43.67
  10.##WILDVALUE##: 121066690
  11.##WILDVALUE##: 12643925
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 2355836987
  17.##WILDVALUE##: 8725486
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 16:
Data sorted by column ID
  1.##WILDVALUE##: 16
  2.##WILDVALUE##: veth497df7f
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 4294967295
  6.##WILDVALUE##: 12:db:e0:82:ca:a2
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 3:03:43.67
  10.##WILDVALUE##: 77756077
  11.##WILDVALUE##: 135696
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 16420222
  17.##WILDVALUE##: 163684
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
Wildvalue: 44:
Data sorted by column ID
  1.##WILDVALUE##: 44
  2.##WILDVALUE##: vethd608288
  3.##WILDVALUE##: 6
  4.##WILDVALUE##: 1500
  5.##WILDVALUE##: 4294967295
  6.##WILDVALUE##: 62:d0:3f:14:ef:85
  7.##WILDVALUE##: 1
  8.##WILDVALUE##: 1
  9.##WILDVALUE##: 2 days, 14:54:42.04
  10.##WILDVALUE##: 1620014
  11.##WILDVALUE##: 4506
  12.##WILDVALUE##: 0
  13.##WILDVALUE##: 0
  14.##WILDVALUE##: 0
  15.##WILDVALUE##: 0
  16.##WILDVALUE##: 6623314
  17.##WILDVALUE##: 18778
  18.##WILDVALUE##: 0
  19.##WILDVALUE##: 0
  20.##WILDVALUE##: 0
  21.##WILDVALUE##: 0
  22.##WILDVALUE##: 0.0
[1, 22, 0.0]
[1, 10, 8962002]
[1, 7, 1]
[1, 18, 0]
[1, 21, 0]
[1, 6, ]
[1, 17, 44266]
[1, 20, 0]
[1, 5, 10000000]
[1, 16, 8962002]
[1, 15, 0]
[1, 4, 65536]
[1, 14, 0]
[1, 3, 24]
[1, 13, 0]
[1, 2, lo]
[1, 12, 0]
[1, 1, 1]
[1, 9, 0:00:00.00]
[1, 11, 44266]
[1, 8, 1]
[1, 19, 0]
[2, 22, 0.0]
[2, 10, 3944686696]
[2, 7, 1]
[2, 18, 0]
[2, 21, 0]
[2, 6, 90:e6:ba:59:1b:18]
[2, 17, 37110637]
[2, 20, 0]
[2, 5, 100000000]
[2, 16, 979342493]
[2, 4, 1500]
[2, 15, 0]
[2, 14, 0]
[2, 3, 6]
[2, 13, 0]
[2, 2, NVIDIA Corporation MCP77 Ethernet]
[2, 12, 2662]
[2, 1, 2]
[2, 9, 0:00:00.00]
[2, 11, 28619544]
[2, 19, 0]
[2, 8, 1]
[3, 22, 0.0]
[3, 10, 0]
[3, 18, 0]
[3, 7, 1]
[3, 21, 0]
[3, 6, 00:50:56:c0:00:01]
[3, 17, 20152]
[3, 20, 0]
[3, 5, 0]
[3, 16, 0]
[3, 4, 1500]
[3, 15, 0]
[3, 3, 6]
[3, 14, 0]
[3, 13, 0]
[3, 2, vmnet1]
[3, 12, 0]
[3, 1, 3]
[3, 9, 0:00:06.31]
[3, 11, 0]
[3, 19, 0]
[3, 8, 1]
[4, 22, 0.0]
[4, 10, 0]
[4, 18, 0]
[4, 7, 1]
[4, 21, 0]
[4, 17, 20150]
[4, 6, 00:50:56:c0:00:08]
[4, 20, 0]
[4, 5, 0]
[4, 16, 0]
[4, 4, 1500]
[4, 15, 0]
[4, 3, 6]
[4, 14, 0]
[4, 2, vmnet8]
[4, 13, 0]
[4, 12, 0]
[4, 1, 4]
[4, 9, 0:00:06.31]
[4, 11, 0]
[4, 19, 0]
[4, 8, 1]
[5, 22, 0.0]
[5, 10, 0]
[5, 18, 0]
[5, 7, 1]
[5, 21, 0]
[5, 17, 0]
[5, 6, 52:54:00:9b:4b:e0]
[5, 20, 0]
[5, 16, 0]
[5, 5, 0]
[5, 4, 1500]
[5, 15, 0]
[5, 3, 6]
[5, 14, 0]
[5, 2, virbr0]
[5, 13, 0]
[5, 1, 5]
[5, 12, 0]
[5, 9, 0:00:09.32]
[5, 11, 0]
[5, 19, 0]
[5, 8, 2]
[6, 8, 2]
[6, 22, 0.0]
[6, 10, 0]
[6, 18, 0]
[6, 7, 2]
[6, 21, 0]
[6, 17, 0]
[6, 6, 52:54:00:9b:4b:e0]
[6, 20, 0]
[6, 16, 0]
[6, 5, 10000000]
[6, 15, 0]
[6, 4, 1500]
[6, 3, 6]
[6, 14, 0]
[6, 2, virbr0-nic]
[6, 13, 0]
[6, 1, 6]
[6, 12, 0]
[6, 9, 0:00:12.32]
[6, 19, 0]
[6, 11, 0]
[7, 8, 1]
[7, 22, 0.0]
[7, 18, 0]
[7, 10, 4161096832]
[7, 7, 1]
[7, 21, 0]
[7, 17, 17462122]
[7, 6, 02:42:39:1b:cc:e3]
[7, 20, 0]
[7, 16, 179090566]
[7, 5, 0]
[7, 15, 0]
[7, 4, 1500]
[7, 14, 0]
[7, 3, 6]
[7, 2, br-6a2604a91ac1]
[7, 13, 0]
[7, 1, 7]
[7, 12, 0]
[7, 9, 3:03:43.67]
[7, 19, 0]
[7, 11, 25238743]
[8, 8, 1]
[8, 22, 0.0]
[8, 18, 0]
[8, 10, 1684285]
[8, 7, 1]
[8, 21, 0]
[8, 17, 30735]
[8, 6, 02:42:55:44:3f:06]
[8, 20, 0]
[8, 16, 17029956]
[8, 5, 0]
[8, 15, 0]
[8, 4, 1500]
[8, 14, 0]
[8, 3, 6]
[8, 13, 0]
[8, 2, docker0]
[8, 1, 8]
[8, 12, 0]
[8, 9, 0:00:18.32]
[8, 19, 0]
[8, 11, 6882]
[10, 4, 1500]
[10, 17, 20997]
[10, 16, 4061394]
[10, 22, 0.0]
[10, 5, 4294967295]
[10, 11, 0]
[10, 10, 0]
[10, 6, 06:31:50:31:a5:fb]
[10, 15, 0]
[10, 1, 10]
[10, 21, 0]
[10, 14, 0]
[10, 20, 0]
[10, 7, 1]
[10, 2, veth92ca0b0]
[10, 19, 0]
[10, 18, 0]
[10, 8, 1]
[10, 13, 0]
[10, 3, 6]
[10, 12, 0]
[10, 9, 0:00:18.32]
[12, 4, 1500]
[12, 17, 8614157]
[12, 16, 2109869568]
[12, 22, 0.0]
[12, 5, 4294967295]
[12, 11, 12459122]
[12, 10, 20649171]
[12, 6, 4e:c1:4e:e6:07:90]
[12, 15, 0]
[12, 21, 0]
[12, 1, 12]
[12, 14, 0]
[12, 20, 0]
[12, 7, 1]
[12, 2, veth2bc8fbd]
[12, 19, 0]
[12, 18, 0]
[12, 8, 1]
[12, 13, 0]
[12, 3, 6]
[12, 12, 0]
[12, 9, 3:03:43.67]
[14, 4, 1500]
[14, 17, 8725486]
[14, 16, 2355836987]
[14, 22, 0.0]
[14, 5, 4294967295]
[14, 11, 12643925]
[14, 10, 121066690]
[14, 6, 0a:2c:4f:3a:eb:5c]
[14, 21, 0]
[14, 15, 0]
[14, 14, 0]
[14, 20, 0]
[14, 1, 14]
[14, 7, 1]
[14, 2, veth9a64fa1]
[14, 19, 0]
[14, 8, 1]
[14, 18, 0]
[14, 13, 0]
[14, 3, 6]
[14, 12, 0]
[14, 9, 3:03:43.67]
[16, 4, 1500]
[16, 17, 163684]
[16, 16, 16420222]
[16, 22, 0.0]
[16, 5, 4294967295]
[16, 11, 135696]
[16, 10, 77756077]
[16, 6, 12:db:e0:82:ca:a2]
[16, 21, 0]
[16, 15, 0]
[16, 20, 0]
[16, 1, 16]
[16, 14, 0]
[16, 7, 1]
[16, 19, 0]
[16, 2, veth497df7f]
[16, 18, 0]
[16, 8, 1]
[16, 13, 0]
[16, 3, 6]
[16, 12, 0]
[16, 9, 3:03:43.67]
[44, 3, 6]
[44, 16, 6623314]
[44, 22, 0.0]
[44, 15, 0]
[44, 9, 2 days, 14:54:42.04]
[44, 21, 0]
[44, 4, 1500]
[44, 10, 1620014]
[44, 5, 4294967295]
[44, 19, 0]
[44, 14, 0]
[44, 20, 0]
[44, 13, 0]
[44, 6, 62:d0:3f:14:ef:85]
[44, 1, 44]
[44, 18, 0]
[44, 17, 18778]
[44, 7, 1]
[44, 12, 0]
[44, 2, vethd608288]
[44, 11, 4506]
[44, 8, 1]
*/
