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

ifEntryRaw = snmpMapToTable(Snmp.walkAsMap(host, "1.3.6.1.2.1.2.2.1", props, timeout))
ifXEntryRaw = snmpMapToTable(Snmp.walkAsMap(host, "1.3.6.1.2.1.31.1.1.1", props, timeout))
ifEntryRaw.each {wildvalue, data ->
    println("Interface ${wildvalue}:\tifAlias: ${ifXEntryRaw[wildvalue]["1"]}\tifDescr: ${data["2"]}\tifType: ${data["3"]}")
}
return 0
