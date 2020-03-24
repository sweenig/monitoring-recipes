/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/
Oid = "1.3.6.1.2.1.2.2.1" //polling the interfaces table (returns "metricOID.wildvalue = datavalue")
wildvalueTerms = 1

import com.santaba.agent.groovyapi.snmp.Snmp

def host = hostProps.get('system.hostname')
def props = hostProps.toProperties()
int timeout = 10000 // 10 sec timeout.

def snmpMapToTable(snmpmap, int wildvalueTerms = 1) {
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

walkResult = Snmp.walkAsMap(host, Oid, props, timeout)
data = snmpMapToTable(walkResult, wildvalueTerms)
println("Raw Result:\n" + "=".multiply(80) + "\n" + walkResult + "\n" + "=".multiply(80))
println("Converted to map:\n" + "=".multiply(80) + "\n" + data + "\n" + "=".multiply(80))

return 0
