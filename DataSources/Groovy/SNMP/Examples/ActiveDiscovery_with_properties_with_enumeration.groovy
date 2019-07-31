/*******************************************************************************
 * © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 * This script can be used as an Auto Discovery script for a multi-instance data
 * source. It returns the list of instances with their name and any defined prop-
 * erties. This is an example only as the functionality covered here is already
 * functional with other mechanisms already built.
 ******************************************************************************/
import com.santaba.agent.groovyapi.snmp.Snmp
enhanceInterfaceData = true //set this to false to omit the parts of this script that are specific to interface polling (you'll still need to )
//parent OID that contains the wildvalue, wildalias, and properties. Usually has the name SomethingSomethingEntry in the MIB.
baseOID = ".1.3.6.1.2.1.2.2.1"

//leaf OID that contains the wildalias, we'll walk this to get the wildvalue and wildalias
aliasOID = "2"

/*******************************************************************************
 * This map contains two elements per entry:
 *     The first is the property name as it will appear in LM properties
 *     The second is a mapping of enumerated values that can be polled from the MIB
 *       mapped to the corresponding meaning.
 * Make sure the keys in both the parent map and in the second element map are strings.
 * Also make sure that properties that do not have an enumeration have a null map
 * as the second element.
 ******************************************************************************/
propstoget = [
  /*******************************************************************************
   These two entries are OIDs that should likely be polled instead of saved as properties because they can change more frequently than the Auto Discovery mechanism would update the property values.
   Nevertheless, they are shown here as examples of OIDs that are enumerated in the MIB. The OIDs "7" and "8" would be appended to the baseOID and polled via SNMP as part of Auto Discovery.
   The resulting values would be checked against the map. If a match is found, the word is returned. If no match is found, the original value is returned.
   The returned values are then stored as properties with the names "ifAdminStatus" and "ifOperStatus".
   ******************************************************************************/
  "7": ["ifAdminStatus",        ["1":"up","2":"down","3":"testing"]                                                                                 ],
  "8": ["ifOperStatus",         ["1":"up","2":"down","3":"testing","4":"unknown","5":"dormant","6":"notPresent","7":"lowerLayerDown"]               ],
  /*******************************************************************************
   These are shown here as examples of OIDs that are NOT enumerated in the MIB.
   The OIDs "4" and "6" would be appended to the baseOID and polled via SNMP as part of Auto Discovery.
   The resulting values would be checked against the map. Since no match will be found (it's an empty map), the original values are returned.
   The returned values are then stored as properties with the names "ifMtu" and "ifPhysAddress".
   ******************************************************************************/
  //"4": ["ifMtu",                [:]                                                                                                                 ],
  //An alternative of the MTU property could look like this:
  "4": ["ifMtu",                ["1500":"Default (1500)"]                                                                                                                 ],
  "6": ["ifPhysAddress",        [:]                                                                                                                 ],
  /*******************************************************************************
   This is an example of an OID that is NOT enumerated in the MIB, but has specific names for common values.
   The OID "5" would be appended to the baseOID and polled via SNMP as part of Auto Discovery.
   The resulting value would be checked against the map. If there happens to be a match, the word is returned.
   If no match is found (i.e. it's an odd speed or just one that hasn't been added here), the original value is returned.
   The returned value is then stored as a property with the name "ifSpeed".
   ******************************************************************************/
  "5": ["ifSpeed",              ["40000000000":"40 Gbps","10000000000":"10 Gbps","1000000000":"1 Gbps","100000000":"100 Mbps","10000000":"10 Mbps"] ],
  /*******************************************************************************
   This is an example of an OID that is enumerated in the MIB. In fact, it has many enumerated values.
   The OID "3" would be appended to the baseOID and polled via SNMP as part of Auto Discovery.
   The resulting value would be checked against the map. If there happens to be a match, the word is returned.
   If no match is found (i.e. it's a new ifType or just one that hasn't been added here), the original value is returned.
   The returned value is then stored as a property with the names "ifType".
   ******************************************************************************/
  "3": ["ifType",
        ["1":"other","2":"regular1822","3":"hdh1822","4":"ddnX25","5":"rfc877x25","6":"ethernetCsmacd","7":"iso88023Csmacd","8":"iso88024TokenBus","9":"iso88025TokenRing",
        "10":"iso88026Man","11":"starLan","12":"proteon10Mbit","13":"proteon80Mbit","14":"hyperchannel","15":"fddi","16":"lapb","17":"sdlc","18":"ds1","19":"e1",
        "20":"basicISDN","21":"primaryISDN","22":"propPointToPointSerial","23":"ppp","24":"softwareLoopback","25":"eon","26":"ethernet3Mbit","27":"nsip","28":"slip","29":"ultra",
        "30":"ds3","31":"sip","32":"frameRelay","33":"rs232","34":"para","35":"arcnet","36":"arcnetPlus","37":"atm","38":"miox25","39":"sonet",
        "40":"x25ple","41":"iso88022llc","42":"localTalk","43":"smdsDxi","44":"frameRelayService","45":"v35","46":"hssi","47":"hippi","48":"modem","49":"aal5",
        "50":"sonetPath","51":"sonetVT","52":"smdsIcip","53":"propVirtual","54":"propMultiplexor","55":"ieee80212","56":"fibreChannel","57":"hippiInterface","58":"frameRelayInterconnect","59":"aflane8023",
        "60":"aflane8025","61":"cctEmul","62":"fastEther","63":"isdn","64":"v11","65":"v36","66":"g703at64k","67":"g703at2mb","68":"qllc","69":"fastEtherFX",
        "70":"channel","71":"ieee80211","72":"ibm370parChan","73":"escon","74":"dlsw","75":"isdns","76":"isdnu","77":"lapd","78":"ipSwitch","79":"rsrb",
        "80":"atmLogical","81":"ds0","82":"ds0Bundle","83":"bsc","84":"async","85":"cnr","86":"iso88025Dtr","87":"eplrs","88":"arap","89":"propCnls",
        "90":"hostPad","91":"termPad","92":"frameRelayMPI","93":"x213","94":"adsl","95":"radsl","96":"sdsl","97":"vdsl","98":"iso88025CRFPInt","99":"myrinet",
        "100":"voiceEM","101":"voiceFXO","102":"voiceFXS","103":"voiceEncap","104":"voiceOverIp","105":"atmDxi","106":"atmFuni","107":"atmIma","108":"pppMultilinkBundle","109":"ipOverCdlc",
        "110":"ipOverClaw","111":"stackToStack","112":"virtualIpAddress","113":"mpc","114":"ipOverAtm","115":"iso88025Fiber","116":"tdlc","117":"gigabitEthernet","118":"hdlc","119":"lapf",
        "120":"v37","121":"x25mlp","122":"x25huntGroup","123":"trasnpHdlc","124":"interleave","125":"fast","126":"ip","127":"docsCableMaclayer","128":"docsCableDownstream","129":"docsCableUpstream",
        "130":"a12MppSwitch","131":"tunnel","132":"coffee","133":"ces","134":"atmSubInterface","135":"l2vlan","136":"l3ipvlan","137":"l3ipxvlan","138":"digitalPowerline","139":"mediaMailOverIp",
        "140":"dtm","141":"dcn","142":"ipForward","143":"msdsl","144":"ieee1394","145":"if-gsn","146":"dvbRccMacLayer","147":"dvbRccDownstream","148":"dvbRccUpstream","149":"atmVirtual",
        "150":"mplsTunnel","151":"srp","152":"voiceOverAtm","153":"voiceOverFrameRelay","154":"idsl","155":"compositeLink","156":"ss7SigLink","157":"propWirelessP2P","158":"frForward","159":"rfc1483",
        "160":"usb","161":"ieee8023adLag","162":"bgppolicyaccounting","163":"frf16MfrBundle","164":"h323Gatekeeper","165":"h323Proxy","166":"mpls","167":"mfSigLink","168":"hdsl2","169":"shdsl",
        "170":"ds1FDL","171":"pos","172":"dvbAsiIn","173":"dvbAsiOut","174":"plc","175":"nfas","176":"tr008","177":"gr303RDT","178":"gr303IDT","179":"isup",
        "180":"propDocsWirelessMaclayer","181":"propDocsWirelessDownstream","182":"propDocsWirelessUpstream","183":"hiperlan2","184":"propBWAp2Mp","185":"sonetOverheadChannel","186":"digitalWrapperOverheadChannel","187":"aal2","188":"radioMAC","189":"atmRadio",
        "190":"imt","191":"mvl","192":"reachDSL","193":"frDlciEndPt","194":"atmVciEndPt","195":"opticalChannel","196":"opticalTransport","197":"propAtm","198":"voiceOverCable","199":"infiniband",
        "200":"teLink","201":"q2931","202":"virtualTg","203":"sipTg","204":"sipSig","205":"docsCableUpstreamChannel","206":"econet","207":"pon155","208":"pon622","209":"bridge",
        "210":"linegroup","211":"voiceEMFGD","212":"voiceFGDEANA","213":"voiceDID","214":"mpegTransport","215":"sixToFour","216":"gtp","217":"pdnEtherLoop1","218":"pdnEtherLoop2","219":"opticalChannelGroup",
        "220":"homepna","221":"gfp","222":"ciscoISLvlan","223":"actelisMetaLOOP","224":"fcipLink","225":"rpr","226":"qam","227":"lmp","228":"cblVectaStar","229":"docsCableMCmtsDownstream",
        "230":"adsl2","231":"macSecControlledIF","232":"macSecUncontrolledIF","233":"aviciOpticalEther","234":"atmbond","235":"voiceFGDOS","236":"mocaVersion1","237":"ieee80216WMAN","238":"adsl2plus","239":"dvbRcsMacLayer",
        "240":"dvbTdm","241":"dvbRcsTdma",]
     ],
]
//You shouldn't have to modify anything after this line
hostname = hostProps.get('system.hostname')
output = [:] //map to contain the instances (one entry per instance, one entry per line output)
data = Snmp.walkAsMap(hostname, baseOID, null) //grab the data via snmp
aliases = Snmp.walkAsMap(hostname, ".1.3.6.1.2.1.31.1.1.1",null) //grab the aliases (that live in a different branch of the MIB)
data.each { key, val -> //loop through the results looking for the aliasOID
  if (key.matches(~/${aliasOID}(\.\d*)+/)) { //if the current line is in the aliasOID,
    wildalias = key.tokenize(".").tail().join(".")
    if(enhanceInterfaceData){
      activity_indicator = Long.parseLong(aliases["6."+wildalias] ?: "0") + Long.parseLong(aliases["10."+wildalias] ?: "0") + Long.parseLong(data["10."+wildalias] ?: "0") + Long.parseLong(data["16."+wildalias] ?: "0")
      output[wildalias] = ["description":val ?: "NODESCR", "alias": aliases["18." + wildalias], "ifName": aliases["1." + wildalias], "activityindicator": activity_indicator] //create an item in the map for this instance, add the alias and description
    } else {
      output[wildalias] = ["alias":val ?: "NODESCR"] //create an item in the map for this instance, add the alias and description
    }
  }
}
output.each {i_key, i_val -> //only search for properties for valid, discovered instances
  props = [] //empty list to contain the properties
  data.each {d_key, d_val -> //inspect each row in the data to see if it's for this instance
    oidstem = d_key.tokenize(".")[0]//strip the last element off the OID
    if (d_key.tokenize(".").tail().join(".") == i_key && propstoget.containsKey(oidstem)) { //if it's for this instance
      props += "${propstoget[oidstem][0]}=${propstoget[oidstem][1][d_val] ?: URLEncoder.encode(d_val)}" //add it to the properties (replace with enumeration if possible)
    }
  }
  if(enhanceInterfaceData){
    println("${i_key}##${i_val['ifName']}##${i_val['alias'] ?: i_val['description']} (${i_key})####description=${i_val['description']}&ifalias=${i_val['alias']}&ifname=${i_val['ifName']}&activityindicator=${i_val['activityindicator']}&" + props.join('&'))
  } else {
    println("${i_key}##${i_key}##${i_val['alias']}####" + props.join('&'))
  }
}
return(0)
