/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/
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

ifEntryRaw = snmpMapToTable(Snmp.walkAsMap(host, "1.3.6.1.2.1.2.2.1", props, timeout))
ifXEntryRaw = snmpMapToTable(Snmp.walkAsMap(host, "1.3.6.1.2.1.31.1.1.1", props, timeout))
ifEntryRaw.each {wildvalue, data ->
    println("Interface ${wildvalue}:\n\tifAlias: ${ifXEntryRaw[wildvalue]["1"]}\n\tifDescr: ${data["2"]}\n\tifType: ${data["3"]}")
}
return 0
