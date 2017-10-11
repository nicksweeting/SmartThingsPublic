definition(
    name: "A.I. Thermostat Manager",
    namespace: "ELFEGE",
    author: "ELFEGE",

    description: """Manage one or more thermostats in parallel with several other features such as: 
- Home location mode (up to 5 modes)
- open/close windows and/or turn on/off fans instead of AC
- contact sensors 
- humidity measurment
- outside / inside temperatures amplitude
- wind speed 
- subjective ("feels like") outside's temperature
- adjust temperature with presence using motion sensors
- switch on/off state
""" ,
    category: "Green Living",
    iconUrl: "http://elfege.com/penrose.jpg",
    iconX2Url: "http://elfege.com/penrose.jpg",
    image: "http://elfege.com/penrose.jpg"
)

preferences {

    page name: "pageSetup"
    page name: "settings"
    page name: "Modes"
    page name: "AI"



}
def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    return dynamicPage(pageProperties) {


        section("""Manage one or more thermostats in parallel with several other features such as:

- Home location mode (up to 5 modes)
- open/close windows and/or turn on/off fans instead of AC
- contact sensors 
- humidity measurment
- outside / inside temperatures amplitude
- wind speed 
- subjective ("feels like") outside's temperature
- adjust temperature with presence using motion sensors
- switch on/off state 
"""){  }
        section("Main Settings") {
            href "settings", title: "Thermostats and other devices", description: ""

            href "Modes", title: "Modes and temperatures", description: ""

        }

        section("Set devices and values for smart management") {
            href "AI", title: "Artificial Intelligence: make this app adjust temperatures with different scenarios", description: ""

        }
        section(){
            if(OutsideTempHighThres){
                input(name:"adjustments", type: "enum", title: "Do you want to use dynamic temperatures adjustments?", 
                      options: ["no, just go with my default settings", 
                                "Yes, use a linear variation", "Yes, but use a logarithmic variation"], required: true)

                paragraph """
linear: save power and money and remain comfortable. 
Algorithmic: save less money but be even more comfortable"""
            }
        }

        section(){
            mode(title: "Set for specific mode(s)")
        }
        section(){
            input(name:"sendPushMessage", type: "bool", title: "Send Push Notification?")
        }
    }
}
def settings() {

    def pageName = "settings"

    def pageProperties = [
        name:       "settings",
        title:      "Thermostats and other devices",
        nextPage:   "pageSetup",
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {

        section("Select the thermostats you want to control") { 

            input(name: "Thermostats", type: "capability.thermostat", title: "select thermostats", required: false, multiple: true, description: null, submitOnChange: true, uninstall: true)
            if(Thermostats){

                input(name: "AltSensor", type: "bool", title: "Control some thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor){
                    input(name: "ThermSensor", type: "enum", title: "Which devices do you want to control this way?", multiple: true, options: ["${Thermostats[0]}", "${Thermostats[1]}", "${Thermostats[2]}"], required: true, submitOnChange: true)

                    def t0 = Thermostats[0]
                    def t1 = Thermostats[1]
                    def t2 = Thermostats[2]
                    def ThermSensorColl = ThermSensor.collect{ it.toString() }
                    def t0Coll = t0.collect{ it.toString() }
                    def t1Coll = t1.collect{ it.toString() }
                    def t2Coll = t2.collect{ it.toString() }
                    def intersect0 = t0Coll.intersect(ThermSensorColl)
                    def intersect1 = t1Coll.intersect(ThermSensorColl) 
                    def intersect2 = t2Coll.intersect(ThermSensorColl)
                    log.debug """ t0Coll = $t0Coll, t1Coll = $t1Coll, t2Coll = $t2Coll
intersect0 = $intersect0, intersect1 = $intersect1, intersect2 = $intersect2"""

                    if(intersect0.size() != 0) {
                        input(name: "Sensor_1", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control ${Thermostats[0]}", required: false, multiple: false, description: null, uninstall: true)
                    }
                    if(intersect1.size() != 0) {
                        input(name: "Sensor_2", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control ${Thermostats[1]}", required: false, multiple: false, description: null, uninstall: true)
                    }
                    if(intersect2.size() != 0) {
                        input(name: "Sensor_3", type: "capability.temperatureMeasurement", title: "Select a third party sensor to control ${Thermostats[2]}", required: false, multiple: false, description: null, uninstall: true)
                    }
                    if(Thermostats.size() > 3){
                        paragraph: "You can control only 3 units with a separate sensor"

                    }
                }
                input(name: "turnOffWhenReached", type: "bool", title: "Turn off thermostats when desired temperature is reached?", required: false, default: false, submitOnChange: true)
            }  
        }
    }
}
def Modes(){

    def pageName = "Modes"

    def pageProperties = [
        name:       "Modes",
        title:      "Modes and temperatures",
        nextPage:   "pageSetup",
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {
        if(Thermostats.size() > 0){
            section("Main Mode") {
                input(name: "Home", type : "mode", title: "Select modes for when you're at home", multiple: true, required: false, submitOnChange: true)
            }
            section("Other Modes"){
                input(name: "Night", type : "mode", title: "Select Night mode", multiple: true, required: true, submitOnChange: true)
                input(name: "Away", type : "mode", title: "Select away mode", multiple: true, required: true, submitOnChange: true)
            }
            section("MoreModes"){ 
                input(name: "Moremodes", type: "bool", title: "add more modes", required: false, defaut: false, submitOnChange: true)
                if(Moremodes){
                    input(name: "CustomMode1", type : "mode", title: "Select mode", multiple: true, required: true, submitOnChange: true)
                    input(name: "CustomMode2", type : "mode", title: "Select mode", multiple: true, required: true, submitOnChange: true)
                }
            }
            section(){
                input(name: "LetAIdoIt", type: "bool", title: "Let A.I. chose best temperatures", required: false, defaut: false, submitOnChange: true)
                def setC = ""
                def setH = ""
                def setCA = ""
                def setHA = ""
                def tsize = Thermostats.size()
                def i = 0
                if(LetAIdoIt){          
                    setC = 72
                    setH = 70
                    setCA = 80
                    setHA = 66
                }



                i = 0
                for(tsize != 0; i < tsize; i++){    
                    if(!LetAIdoIt){
                        input(name: "HSPH${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $Home mode", required: true)
                        input(name: "CSPH${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $Home mode", required: true)
                        input(name: "HSPN${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $Night mode", required: true)
                        input(name: "CSPN${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $Night mode", required: true)
                        if(Moremodes){
                            input(name: "HSPCust1_T${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $CustomMode1 mode", required: true)
                            input(name: "CSPCust1_T${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $CustomMode1 mode", required: true)
                            if(CustomMode2){
                                input(name: "HSPCust2_T${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $CustomMode2 mode", required: true)
                                input(name: "CSPCust2_T${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $CustomMode2 mode", required: true)
                            }
                        }
                    }
                    else {
                        input(name: "HSPH${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $Home mode", required: true,  defaultValue: setH)
                        input(name: "CSPH${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $Home mode", required: true, defaultValue: setC)
                        input(name: "HSPN${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $Night mode", required: true, defaultValue: setH)
                        input(name: "CSPN${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $Night mode", required: true, defaultValue: setC)
                        if(Moremodes){
                            input(name: "HSPCust1_T${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $CustomMode1 mode", required: true, defaultValue: setH)
                            input(name: "CSPCust1_T${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $CustomMode1 mode", required: true, defaultValue: setC)
                            if(CustomMode2){
                                input(name: "HSPCust2_T${i}", type: "decimal", title: "Set Heating temperature for ${Thermostats[i]} in $CustomMode2 mode", required: true, defaultValue: setH)
                                input(name: "CSPCust2_T${i}", type: "decimal", title: "Set Cooling temperature for ${Thermostats[i]} in $CustomMode2 mode", required: true, defaultValue: setC)
                            }
                        }
                    }         
                }
                paragraph "Away modes' values will apply to all thermostats evenly"
                input(name: "HSPA", type: "decimal", title: "Set Heating temperature for $Away mode", required: true, defaultValue: setHA)
                input(name: "CSPA", type: "decimal", title: "Set Cooling temperature for $Away mode", required: true, defaultValue: setCA)
            }
        }
        else {
            section("You must first set at least one thermostat in the settings page before you can define any setting here"){}
        }
    }
}
def AI() {

    def pageName = "AI"

    def pageProperties = [
        name:       "AI",
        title:      "Smart Management",
        nextPage:   "pageSetup",
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {
        section("Turn off thermostats when these contacts are open"){

            input(name: "Maincontacts", type:"capability.contactSensor", title: "Turn off all units when these contacts are open", multiple: true, required: false, submitOnChange: true)
            input(name: "ContactAndSwitch", type: "capability.switch", title: "And also control these switches", multiple: true, required: false, submitOnChange: true)

            if(Maincontacts){
                input(name: "TimeBeforeClosing", type: "number", title: "Turn off units after this amount of time when contacts are open", required: false, description: "time in seconds", uninstall: true, install: true)
                input(name: "CriticalTemp", type:"number", title: "but do not allow the temperature to fall bellow this value", required: true, decription: "Enter a safety temperature value")
                input(name: "XtraTempSensor", type:"capability.temperatureMeasurement", title: "select a temperature sensor that will serve as reference", required: true, submitOnChange: true)			
            }
            if(ContactAndSwitch){
                input(name: "ToggleBack", type: "bool", title: "Turn $ContactAndSwitch back on once all windows are closed", default: true, submitOnChange: true)
                if(ToggleBack){
                    input(name: "SwitchIfMode", type: "bool", title: "Keep $ContactAndSwitch off at all times when in certain modes", default: false, submitOnChange: true)
                    if(SwitchIfMode){
                        input(name: "SwitchMode", type : "mode", title: "Select modes", multiple: true, required: true, submitOnChange: true)
                    }

                    input "ContactAndSwitchInSameRoom", "bool", title: "$ContactAndSwitch is in the same room as one of my other HVAC units", default: false, submitOnChange: true
                    if(ContactAndSwitchInSameRoom){
                        input "UnitToIgnore", "capability.thermostat", title: "When $ContactAndSwitch is ON, turn off these units", description: "highly recommended if in the same room", required: true, multiple: true
                    }
                }
            }
        }
        section("Exception Thermostat"){
            def MyThermostats = []
            Thermostats.each {MyThermostats << "$it"}
            input(name: "NoTurnOffOnContact", 
                  type: "enum", 
                  options: MyThermostats.sort(),
                  multiple: true, 
                  title: "Do not turn off this unit upon contacts events", 
                  submitOnChange: true, 
                  required: false,
                  description: "select which unit ")

            if(NoTurnOffOnContact){
                def XtraTempSensorColl = XtraTempSensor.collect{ it.toString() }
                def NoTurnOffOnContactColl = NoTurnOffOnContact.collect{ it.toString() }
                def Intersection = XtraTempSensorColl.intersect(NoTurnOffOnContactColl)
                log.debug "Intersection: ${Intersection}"

                def Same = Intersection.size() != 0
                log.debug "SAME = $Same"
                if(Same){
                    log.debug "YES"
                    paragraph """WARNING! You chose $XtraTempSensor as a device to measure inside's temperature AND as the thermostat which will not shut down when 
windows are open. This will prevent windows from opening when outside's temperature is lower than in other rooms but not in this one (since AC may still run in this room)
It is highly recommended to select another device or this app will not be capable of handling temperatures properly"""
                }

                paragraph "Under which modes you want this exception to apply? "
                input(name: "DoNotTurnOffModes", type : "mode", title: "Select which modes", 
                      /*options: ["$Home", "$Night", "$Away", "${CustomMode1[0]}", "${CustomMode2[0]}"], */
                      multiple: true, required: true)
                input(
                    name: "ContactException", 
                    type : "capability.contactSensor", 
                    multiple: true, title: "unless these specific contacts sensors are open", 
                    description: "Select a contact", 
                    required: false, 
                    submitOnChange: true
                )

            }


            if(NoTurnOffOnContact && ContactException && Actuators){

                input(
                    name: "ActuatorException", 
                    type: "capability.switch", 
                    required: false, 
                    multiple: true, 
                    title: "Select the windows' actuator (or fans' switches) associated to this exception contact sensor",
                    submitOnChange: true
                )
                // build strings for message without []
                if(ActuatorException){
                    def MessageStr = new StringBuilder();
                    for (String value : Actuators) {
                        MessageStr.append(value);
                    }
                    def ActuatorsSTr = MessageStr.toString();

                    def MessageStr2 = new StringBuilder();
                    for (String value : ActuatorException) {
                        MessageStr2.append(value);
                    }
                    def ActuatorExceptionSTr = MessageStr2.toString();

                    def MessageStr3 = new StringBuilder();
                    for (String value : ContactException) {
                        MessageStr3.append(value);
                    }
                    def ContactExceptionSTr = MessageStr3.toString();


                    paragraph """
$ActuatorExceptionSTr will not open with wheather like $ActuatorsSTr. 
But it will close with $ActuatorsSTr. 
$ContactExceptionSTr, when open, will turn off AC or heater but $Maincontacts won't. 

"""
                }
            }
            if(ContactException){
                paragraph """
You selected $ContactAndSwitch to be controled upon contact events.
Do you wish to bind it to the same rule and have it controled exclusively with $ContactException events?"""

                input(name: "FollowException", type: "bool", title: "yes? no?", default: false, submitOnChange: true)
            }
        }
        section("Select an outside sensor"){
            paragraph """
This sensor is essential to the A.I. of this app. 
If you do not have an outside temperature measurment device, you can 
allways create a SmartWeater virtual device. It is actually recommended 
for it is more reliable in many aspects than a physical device located outside
Visit Smartthings community which contains many pages indicating how to proceed step by step"""

            input(name: "OutsideSensor", type: "capability.temperatureMeasurement", title: "Pick a sensor for Outside's temperature", required: true, multiple: false, description: null, submitOnChange: true)        

            def hasHumidity = OutsideSensor?.hasAttribute("humidity")
            //log.debug "hasHumidity is $hasHumidity .."
            if(hasHumidity){
                //input(name: "HumidityMeasurement", type: "capability.relativeHumidityMeasurement", title: "Pick an outside humidity sensor", required: true, multiple: false, description: null)
                input(name: "HumidityTolerance", type: "number", title: "set a humidity tolerance threshold", required: true, description: "set a humidity threshold")
                paragraph """
This level of humidity will determine how to modulate several values such as, for example, 
cooling set points. The more humid, the more the AC will be 'sensitive' 
and run, the less humid, the less it'll run (and if this option has 
been picked elsewhere, it'll prefer to open the windows or activate a fan)"""
            }
        }
        section("Keep a unit running when a contact sensor is CLOSED"){
            input(name: "KeepACon", type: "bool", title: "Use specific settings when a different contact is CLOSED", default: false, submitOnChange: true, description: "")
            if(KeepACon){

                input(name: "ThermContact", type: "capability.thermostat", required: true, title: "Select a Thermostat", submitOnChange: true, description: "")

                if(ThermContact){
                    input "BedSensor", "capability.contactSensor", title: "Select a contact sensor", multiple: true, required: true, description: "$descript", submitOnChange: true 
                    def MainCon = Maincontacts.collect{ it.toString() }
                    def BedSen = BedSensor.collect{ it.toString() }
                    def SensorIntersection = MainCon.intersect(BedSen)
                    log.debug "SensorIntersection: ${SensorIntersection}"
                    if(SensorIntersection.size() != 0){
                        log.debug "WRONG DEVICE"
                        paragraph "WRONG DEVICE! You selected a contact that is already being used by this app. Please select a different contact or uncheck this option" 
                    }
                    input(name: "HSPSetBedSensor", type: "decimal", title: "Set Heating temperature", required: true)
                    input(name: "CSPSetBedSensor", type: "decimal", title: "Set Cooling temperature", required: true)
                }
                input "falseAlarmThreshold", "decimal", title: "False alarm threshold", required: false, description: "Number of minutes (default is 2 min)"

            }
        }
        section("Modify setpoints with a switch on/off status"){  
            input(name: "ExceptionSW", type : "bool", 
                  title: "Apply different settings for a specific thermostat when a switch is on", 
                  defaut: false, 
                  submitOnChange: true)

            if(ExceptionSW){  
                def MyThermostats = []
                Thermostats.each {MyThermostats << "$it"}
                input(name: "ExceptionSwTherm", 
                      type: "enum", 
                      options: MyThermostats.sort(),
                      multiple: false, 
                      title: "", 
                      submitOnChange: true, 
                      description: "select which thermostat")

                input(name: "warmerorcooler", type : "enum", title: "Have this room Warmer or cooler?", 
                      required: true, options: ["warmer", "cooler", "more heating, cooler cooling"], submitOnChange: true)
                input(name: "CtrlSwt", type: "capability.switch", title: "Adjust $ExceptionSwTherm settings When this switch is on", required: true, submitOnChange: true)


                if(warmerorcooler == "more heating, cooler cooling"){
                    input(name: "AddDegrees", type: "decimal", title: "Add this value to $ExceptionSwTherm heat setting When $CtrlSwt is on", required: true, range: "1..5")
                    input(name: "SubDegrees", type: "decimal", title: "Substract this value to $ExceptionSwTherm cooling setting When $CtrlSwt is on", required: true, range: "1..5")                     
                }
                else if(warmerorcooler == "warmer"){
                    input(name: "AddDegrees", type: "decimal", title: "Add this value to $ExceptionSwTherm for both cooling and heating settings When $CtrlSwt is on", required: true, submitOnChange: true, range: "1..5")
                    def set = AddDegrees?.toInteger()
                    input(name: "SubDegrees", type: "decimal", title:"Enter here the same value than above", description: "enter here the same value than above", required: true, defaultValue: set, range: "1..5")  
                    if(AddDegrees){
                        log.info "SubDegrees = $SubDegrees"
                    }
                }
                else if(warmerorcooler == "cooler"){        
                    input(name: "SubDegrees", type: "decimal", title: "Substract this value to $Thermostat_1 for both cooling and heating settings When $CtrlSwt is on", required: true, submitOnChange: true, range: "1..5")
                    def set = SubDegrees?.toInteger()
                    input(name: "AddDegrees", type: "decimal", title:"Enter the same value", description: "enter here the same value than above", required: true, defaultValue: set, range: "1..5")  
                    if(AddDegrees){
                        log.info "AddDegrees = $AddDegrees"
                    }
                }
            }
        }
        section("Save power by turning on some fans or by opening some windows when outside's temperature is nice"){
            paragraph "this section is optimized for windows management but can also be used with fans"
            input(name: "Actuators", type: "capability.switch", required: false, multiple: true, title: "select some fan or windows switches that you want to control with outside's temperature", submitOnChange: true)
            def HasStop = Actuators?.hasCommand("stop") || Actuators?.hasCommand("Stop") 
            if(HasStop){
                input(name: "OperatingTime", type: "number", title: "Should I stop opening operation after this amount of time?", required: false, description: "time in seconds")
            }       
            if(Actuators){
                input(name: "OutsideTempLowThres", type: "number", title: "Outside temperature above which I open windows/turn on Fans", required: true, description: "Outside Temp's Low Threshold")
                input(name: "OutsideTempHighThres", type: "number", title: "Outside temperature above which I keep windows/fans closed/off", required: true, description: "Outside Temp's High Threshold")
                input(name: "ExceptACMode1", type: "mode", title: "if location is in this mode, lower outside temperature High Threshold", required: false, multiple: false, submitOnChange: true)
                if(ExceptACMode1){
                    input(name: "ExceptHighThreshold1", type: "number", title: "pick an offset value for $ExceptACMode1 mode", required: true)
                }
                input(name: "ExceptACMode2", type: "mode", title: "if location is in this mode, lower Outside Temp's High Threshold", required: false, multiple: false, submitOnChange: true)
                if(ExceptACMode2){
                    input(name: "ExceptHighThreshold2", type: "number", title: "pick an offset value for $ExceptACMode2 mode", required: true)
                }
                input(name: "OffSet", type: "decimal", title: "You set Critical Temp at: ${CriticalTemp}. Close windows / turn off fans when inside temp is inferior or equal to this value + OffSet ", required: true, description: "Set OffSet Value")
                paragraph """
If within margin then open windows / turn on fans, but not if inside's temp is lower than heat setting minus offset. 
Reference measurment is taken from $XtraTempSensor. You may also chose to open windows at full lenght whenever outside's temperature 
allows for it, instead of only when cooling is required (see below)"""
                input(name: "OpenWhenEverPermitted", type: "bool", title: "Open in full whenever it is nice outside?", default: false, submitOnChange: true)
                input(name: "OpenInfullWhenAway", type: "bool", title: "Open in full whenever you're away and it's not cold outside?", default: false, submitOnChange: true)
                if(OpenInfullWhenAway){
                    paragraph "Be aware that you have selected $Away as your AWAY mode. Make sure this is the mode for when there's nobody home"
                }
            }
        }
        section("Micro Location"){
            def MyThermostats = []
            Thermostats.each {MyThermostats << "$it"}
            input(name: "useMotion", type: "bool", title: "Use motion sensors to adjust Thermostats settings when inactive", submitOnChange: true, default: false)

            if(useMotion){
                // list all modes minus $Away
                def i = 0
                state.modes = []
                def allModes = location.modes
                def amS = allModes.size()
                log.debug "allModes size: = ${amS}"

                for(amS != 0; i < amS; i++){
                    if(allModes[i] in Away){
                        log.debug "${allModes[i]} Skipped"
                    }
                    else {
                        state.modes << ["${allModes[i]}"]
                    }
                }

                log.debug "all location modes available are: = $state.modes"

                input(
                    name: "howmanySensors", 
                    type: "number", 
                    title: "How many sensors do you want to add overall?", 
                    required: true, 
                    submitOnChange: true, 
                    defaultValue: "${Thermostats.size()}"
                )

                log.debug "thermMotion list is : $thermMotion"
                def ms = howmanySensors

                input(name: "MotionSensor", type: "capability.motionSensor", 
                      multiple: true, 
                      title: "Select the sensors to use with your thermostats ", 
                      description: "pick a sensor", 
                      required: true, 
                      submitOnChange: true,

                     )

                if(MotionSensor){
                    input(name: "HeatNoMotion", type: "number", title: "Substract this amount of degrees to heat setting", required: true, defaultValue: 2)
                    input(name: "CoolNoMotion", type: "number", title: "Add this amount of degrees to cooling setting", required: true, defaultValue: 2)  
                    i = 0
                    for(ms > 0; i < ms; i++){
                        paragraph "Now Select which thermostats will work with which sensors"
                        input(name: "thermMotion${i}", type: "enum",
                              title: "Use ${MotionSensor[i]} to adjust this thermostat's settings when inactive", 
                              description: "select which thermostat you want to control this way",
                              options: MyThermostats.sort(),
                              multiple: false,
                              required: true,
                              /*defaultValue: "${Thermostats[i]}"*/

                             )


                        input(
                            name: "MotionModes${i}", type: "mode", 
                            title: "Use ${MotionSensor[i]} only if home is in these modes", 
                            multiple: true, 
                            description: "select a mode", 
                            required: true, 

                            /*defaultValue: "${state.modes}"*/
                        )



                    }

                    input (name:"minutesMotion", type:"number", title: "For how long there must be no motion for those settings to apply? ", 
                           range: "2..999", 
                           description: "time in minutes",
                           required: false)
                    paragraph "this timer will apply indifferently to all selected motion sensors"

                }
            }
        }
    }
}

// install and updated
def installed() {	 
    // //log.debug "enter installed, state: $state"	
    state.windowswereopenandclosedalready = false // this value must not be reset by updated() because updated() is run by contacthandler

    // default values to avoid NullPointer // must be set as such only for new installation not in init or updated  

    state.humidity = HumidityTolerance - 1

    //log.debug "state.humidity is $state.humidity (updated() loop)"
    state.wind = 4
    state.FeelsLike = OutsideSensor?.latestValue("feelsLike")

    state.OpenByApp = true
    state.ClosedByApp = true // these values must not be reset with updated, only here and modeCHangeHandler

    // first default values to be set to any suitable value so it doesn't crash with null value 
    // they will be updated within seconds with user's settings 

    def t = Thermostats.size()
    def loopV = 0
    for(t != 0; loopV < t; loopV++){
        state."{newValueT${loopV}CSP}" = 75
        state."{newValueT${loopV}HSP}" = 75
    }


    init()
}
def updated() {
    state.modeStartTime = now() 

    state.LastTimeMessageSent = now() as Long // for causes of !OkToOpen message

    log.info "updated with settings = $settings"


    unsubscribe()
    unschedule()
    EndEvalTRUE() 

    init()
}
def init() {

    state.now = now()
    state.CSPSet = 72
    state.HSPSet = 72 // temporary values to prevent null error

    subscribe(Maincontacts, "contact.open", contactHandlerOpen)
    subscribe(Maincontacts, "contact.closed", contactHandlerClosed)

    if(ContactException){
        subscribe(ContactException, "contact.open", contactExceptionHandlerOpen)
        subscribe(ContactException, "contact.closed", contactExceptionHandlerClosed)
        //  subscribe(ContactException, "contact.open", contactHandlerOpen)
        // subscribe(ContactException, "contact.closed", contactHandlerClosed)
        //log.debug "subscribed ContactException to ContactException Handler"     
    }

    subscribe(XtraTempSensor, "temperature", temperatureHandler)
    subscribe(location, "mode", ChangedModeHandler)	

    if(BedSensor){
        subscribe(BedSensor, "contact.open", BedSensorHandler)
        subscribe(BedSensor, "contact.closed", BedSensorHandler)

    }

    subscribe(OutsideSensor, "temperature", temperatureHandler)
    def hasHumidity = OutsideSensor.hasAttribute("humidity")
    //log.debug "hasHumidity is $hasHumidity"
    if(hasHumidity){
        subscribe(OutsideSensor, "humidity", HumidityHandler)

    }
    def hasFeelsLike = OutsideSensor.hasAttribute("feelsLike")
    if(hasFeelsLike){
        subscribe(OutsideSensor, "feelsLike", FeelsLikeHandler)

    }
    def hasWind = OutsideSensor.hasAttribute("wind")
    if(hasWind){
        subscribe(OutsideSensor, "wind", WindHandler)

    }


    def loopV = 0
    def t = Thermostats.size()
    for(t > 0; loopV < t; loopV++){

        hasHumidity = Thermostats[loopV].hasAttribute("humidity")
        if(hasHumidity){
            subscribe(Thermostats[loopV], "humidity", InsideHumidityHandler)
        }
        subscribe(Thermostats[loopV], "heatingSetpoint", setpointHandler)
        subscribe(Thermostats[loopV], "coolingSetpoint", setpointHandler)
        subscribe(Thermostats[loopV], "temperature", temperatureHandler)
        subscribe(Thermostats[loopV], "thermostatMode", ThermostatSwitchHandler)

        log.debug "init therm ${Thermostats[loopV]} = loop $loopV"
    }


    if(ExceptionSW){
        if(CtrlSwt){
            subscribe(CtrlSwt, "switch", switchHandler)
        }
    }
    if(ContactAndSwitch){

        subscribe(ContactAndSwitch, "switch", ContactAndSwitchHandler)

    }
    if(AltSensor){

        if(Sensor_1){
            subscribe(Sensor_1, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor for $Sensor_1"
        }
        if(Sensor_2){
            subscribe(Sensor_2, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor for $Sensor_2"
        }
        if(Sensor_3){
            subscribe(Sensor_3, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor for $Sensor_3"
        }
    }

    loopV = 0
    def ms = MotionSensor.size()
    for(mst > 0; loopV < ms; loopV++){    
        subscribe(MotionSensor[loopV], "motion", motionSensorHandler)
        log.debug "${MotionSensor[loopV]} subscribed to evt"    
    }

    if(Actuators){
        subscribe(Actuators, "switch", switchHandler)
    }
    if(ActuatorException){
        subscribe(ActuatorException, "switch", switchHandler)
    }


    state.doorsAreOpen = false


    state.messageOkToOpenCausesSent = 0

    def ContactsClosed = AllContactsAreClosed()
    //log.debug "enter updated, state: $state"  


    state.messageSent = 0

    state.locationModeChange = true 



    log.debug "Number of Thermostats Selected by User : ${Thermostats.size()} [init]"

    runIn(10, resetLocationChangeVariable)

    // reset A.I. override maps
    state.HSPMap = [:]
    state.CSPMap = [:]


    loopV = 0
    t = Thermostats.size()

    state.AppMgtMap = [:]
    def Therm = null
    // here we create the first map of thermostats' overrides values
    // and set them all to true
    for(t > 0; loopV < t; loopV++){
        Therm = Thermostats[loopV]
        state.AppMgtMap << ["$Therm" : true] 
        log.debug "state.AppMgtMap = $state.AppMgtMap"
    }
    /// now we test that the map can change these values correctly without adding up new values

    loopV = 0
    //state.AppMgtMap = [:] 
    def Map = state.AppMgtMap
    def b = "true"
    for(t > 0; loopV < t; loopV++){

        Therm = Thermostats[loopV]
        /// delete the old value
        state.AppMgtMap.remove("$Therm")
        // set the new value for this thermostat
        state.AppMgtMap["$Therm"] = false

        //state.AppMgtMap = Map
        log.info "MODIFICATION TEST: state.AppMgtMap = $state.AppMgtMap"


    }

    SetListsAndMaps()
    schedules()


}

def SetListsAndMaps(){

    // empty previous lists and maps
    state.thermMotion = []              
    state.MotionModes = []

    state.SensorThermMap = [:]

    // recollect lists for motion sensors and associated thermostats
    def loopV = 0
    def s = MotionSensor.size()
    def refnmheatV = ""
    def refnmcoolV = ""
    def reftm = ""
    def refmmodes = ""

    for(s > 0; loopV < s; loopV++){

        reftm = "thermMotion${loopV.toString()}"
        refmmodes = "MotionModes${loopV.toString()}"

        reftm = settings.find{it.key == "$reftm"}
        refmmodes = settings.find{it.key == "$refmmodes"}

        reftm = reftm?.value
        refmmodes = refmmodes?.value

        state.thermMotion << ["$ref"]
        state.MotionModes << ["$refmmodes"]
        //def thermMotion = state.thermMotion
        //    state.SensorThermMap << ["${thermMotion[i]}" : "${MotionSensor[i]}"]

        log.debug """ loop[${i}]
reftm is $reftm
refmmodes is $refmmodes
state.SensorThermMap = $state.SensorThermMap
"""     
    }

    log.debug """
state.thermMotion = $state.thermMotion
state.MotionModes = $state.MotionModes

"""  
}

// MAIN LOOP
def Evaluate(){
    log.trace "CSPA = $CSPA"
    EndEvalFALSE() ///// 

    MotionTest() /// FOR TEST ONLY COMMENT OUT AFTERWARD

    if(state.nextLoopResetsMaps == true){
        state.HSPMap = [:]
        state.CSPMap = [:]
        log.debug "Maps Reset to 0"
    }

    def CurrMode = location.currentMode
    log.debug "Location is in $CurrMode mode"

    def doorsOk = AllContactsAreClosed()
    def ContactExceptionIsClosed = ExcepContactsClosed()
    log.debug "doorsOk?($doorsOk), ContactExceptionIsClosed?($ContactExceptionIsClosed)"

    def CurrentSwitch = ContactAndSwitch.currentSwitch
    def SwState = CurrentSwitch.findAll { switchVal ->
        switchVal == "off" ? true : false
    }

    log.trace "SwState.size() = ${SwState.size()}, ToggleBack = $ToggleBack, doorsOk = $doorsOk, state.turnedOffByApp = $state.turnedOffByApp"
    def contactClosed = false
    def InExceptionContactMode = location.currentMode in DoNotTurnOffModes
    log.debug "InExceptionContactMode = $InExceptionContactMode DoNotTurnOffModes = $DoNotTurnOffModes"

    if(ContactException && FollowException && InExceptionContactMode){
        contactClosed = ExcepContactsClosed() 
    }
    else{
        contactClosed = AllContactsAreClosed()
    }
    log.debug "contactClosed = $contactClosed"
    def inAwayMode = CurrMode in Away

    if(!DoNotTurnBackOnMode() && contactClosed && ToggleBack){  //  && state.turnedOffByApp == true){  
        if(SwState.size() != 0){
            ContactAndSwitch.on()
            log.debug "$ContactAndSwitch TURNED ON"
            state.turnedOffByApp == false
        }else {
            log.debug "$ContactAndSwitch already on"
        }
    }
    else if(DoNotTurnBackOnMode()){
        if(SwState.size() == 0){
            ContactAndSwitch.off()
            log.debug "$ContactAndSwitch TURNED OFF"
            state.turnedOffByApp == true
        }
        else {
            log.debug "$ContactAndSwitch already off"
        }        
    }
    else if(!contactClosed || inAwayMode){
        if(SwState.size() == 0){
            ContactAndSwitch.off()
        }
    }

    def outsideTemp = OutsideSensor?.currentValue("temperature") //as double
    //outsideTemp = Double.parseDouble(outsideTemp)
    outsideTemp = outsideTemp.toInteger()
    def Outside = outsideTemp as int


        if(doorsOk || ContactExceptionIsClosed ){

            log.trace """
Override (AppMgt) modes map: $state.AppMgtMap"""


            def CurrTemp_Alt1 = Sensor_1?.currentValue("temperature")
            def CurrTemp_Alt2 = Sensor_2?.currentValue("temperature")
            def CurrTemp_Alt3 = Sensor_3?.currentValue("temperature")

            def CurrTempList_Alt = [CurrTemp_Alt1, CurrTemp_Alt2, CurrTemp_Alt3]
            log.debug "CurrTempList_Alt = $CurrTempList_Alt"

            def AltSensorDevicesList =  [Sensor_1, Sensor_2, Sensor_3]
            def i = AltSensorDevicesList.size()
            def loopV = 0
            /*

for(i > 0; loopV < i; loopV++) {
if(AltSensorDevicesList[loopV] == null){
remove(AltSensorDevicesList[loopV])
}
}*/

            def AltSensorBoolList = [false, false, false]
            log.debug "BEFORE: AltSensorBoolList = $AltSensorBoolList, AltSensorDevicesList = $AltSensorDevicesList"

            for (i > 0; loopV < i; loopV++) {
                if(AltSensorDevicesList[loopV] != null){
                    AltSensorBoolList[loopV] = true
                    log.debug "AltSensorBoolList[${loopV}] set to true"
                }        
            }

            log.debug "AFTER: AltSensorBoolList = $AltSensorBoolList, AltSensorDevicesList = $AltSensorDevicesList"

            def inCtrlSwtchMode = CurrMode in ["$Home", "$Night", "${CustomMode1[0]}", "${CustomMode2[0]}"]

            if(inCtrlSwtchMode){
                def SwitchesOnTest = CtrlSwt?.currentValue("switch") == "on"
                SwitchesOn = SwitchesOnTest && ContactExceptionIsClosed
                log.debug "SwitchesOn($SwitchesOn)"
            }


            def HSPSet = 0
            def CSPSet = 0
            def LatestThermostatMode = null

            def ThermDeviceList = Thermostats
            def OutsideTempHighThres = ExceptACModes()

            def CurrTempDevice = 0

            def loopValue = 0
            def t = Thermostats.size()

            ///// FOR LOOP
            for(t > 0; loopValue < t; loopValue++){

                log.debug "FOR LOOP $loopValue"

                def ThermSet = Thermostats[loopValue]
                def CurrTemp = ThermSet.currentValue("temperature") 
                def ThermState = ThermSet.currentValue("thermostatMode")  

                def ModeValueList = IndexValueMode()
                def ModeValue = ModeValueList[0]
                def Test = ModeValueList[1]
                log.debug "@ loop ${loopValue}, ModeValue is $ModeValue &&& Test = $Test"

                if(CurrMode in Away){
                    HSPSet = "$HSPA"
                    HSPSet =  HSPSet.toInteger()
                    CSPSet = "$CSPA"
                    CSPSet = CSPSet.toInteger()
                    log.debug """

HSPSet for $ThermSet is $HSPSet
CSPSet for $ThermSet is $CSPSet

"""
                } 
                else {
                    def HSP = "HSP${ModeValue}${loopValue.toString()}"
                    log.debug """HSP is $HSP """

                    //log.trace"SETTINGS: $settings"

                    HSPSet = settings.find{it.key == "$HSP"} // retrieve the String from settings
                    log.debug """HSPSet is $HSPSet (before collection) loop = $loopValue"""
                    HSPSet = HSPSet.value
                    log.debug "HSPSet for $ThermSet is $HSPSet "
                    HSPSet = HSPSet.toInteger()

                    def CSP = "CSP${ModeValue}${loopValue.toString()}"
                    log.debug """CSP is $CSP """
                    
                    CSPSet = settings.find {it.key == "$CSP"} // retrieve from settings
                    log.debug """CSPSet is $CSPSet (before collection) loop = $loopValue"""
                    CSPSet = CSPSet.value
                    log.debug "CSPSet for $ThermSet is $CSPSet"
                    CSPSet = CSPSet.toInteger()
                }
                // end of collection for modes other than Away


                log.debug "state.AppMgtMap = $state.AppMgtMap"
                def AppMgt = state.AppMgtMap.find{it.key == "$ThermSet"}
                AppMgt = AppMgt?.value
                log.debug "AppMgt = $AppMgt"

                def Inside = ThermSet.currentValue("temperature")
                //Inside = Double.parseDouble(Inside)
                //Inside = Inside.toInteger()

                state.Inside = Inside
                //def humidity = OutsideSensor?.latestValue("humidity")
                def humidity = OutsideSensor.currentValue("temperature")
                //humidity = Double.parseDouble(humidity)
                //humidity = humidity.toInteger()

                def TooHumid = humidity > HumidityTolerance && Outside > CSPSet 

                def INSIDEhumidity = ThermSet.latestValue("humidity")   
                //INSIDEhumidity = INSIDEhumidity?.toInteger()
                def TooHumidINSIDE =  INSIDEhumidity > HumidityTolerance 

                log.trace """        
ThermsInvolved = ${Thermostats.size()} 
loop($loopValue) 
AppMgtList = $state.AppMgtMap
AppMgt = $AppMgt
"""        

                state.ThermSet = ThermSet as String
                log.debug "--------------------"

                state.EventAtTempLoop = ThermSet as String // used for false override prevention
                log.debug "ThermSet = $ThermSet - state.EventAtTempLoop = $state.EventAtTempLoop"

                def AltSensor = AltSensorBoolList[loopValue] 

                if(AltSensor){

                    CurrTemp = CurrTempList_Alt[loopValue] 

                    CurrTempDevice = AltSensorDevicesList[loopValue] 
                    log.debug "$CurrTempDevice selected as CurrTemp source for $ThermSet and it returns a temperature of $CurrTemp F"
                }
                else {
                    //CurrTemp = CurrTempList[loopValue] 
                    //CurrTempDevice = AltSensorDevicesList[loopValue]
                    log.debug " $ThermSet returns a temperature of $CurrTemp F"
                }

                //CurrTemp = Double.parseDouble(CurrTemp)
                //CurrTemp = CurrTemp.toInteger()

                log.debug "CurrTemp = $CurrTemp ($ThermSet)"
                state.CurrTemp = CurrTemp

                // motion management
                log.debug "-------------------------------------------------------------------------------------------------------------------------------------"
                if(useMotion){
                    log.debug """
MotionModes are : $state.MotionModes
MotionSensors are $state.MotionSensor
"""

                    def MotionSensorList = MotionSensor.findAll{it.device != null}.sort() 
                    def MotionSensor = MotionSensorList[loopValue] 

                    //def MotionModesList = state.MotionModes.findAll{it.device != null}.sort() 
                    def MotionModes = state.MotionModes[loopValue]

                    /// TO FIX : MotionModes = MotionModes[loopValue]
                    def InMotionModes = CurrMode in MotionModes 
                    state.InMotionModes = InMotionModes

                    def HeatNoMotionVal = HeatNoMotion
                    def CoolNoMotionVal = CoolNoMotion
                    def ActiveList = MotionTest() 
                    def Active = ActiveList[loopValue]
                    log.debug """
MotionModes = $state.MotionModes, 
InMotionModes?($InMotionModes),
useMotion?($useMotion)
ActiveList = $ActiveList
MotionSensorList = $MotionSensorList
Active?(from List) for $ThermSet && $MotionSensor = $Active
"""
                }


                def defaultCSPSet = CSPSet // recording this default value so if A.I. brings setpoint too low, it'll be recovered
                def defaultHSPSet = HSPSet // same but with heat

                //CSPSet = CSPSet.toInteger()


                log.debug """
Current Temperature Inside = $Inside
//log.debug "ShouldHeat = $ShouldHeat
"""

                /// ALGEBRA

                def xa = 0
                def ya = 0

                def xb = 0
                def yb = 0
                def b = 0
                def coef = 0


                if(adjustments == "Yes, use a linear variation"){
                    /////////////////////////COOL////////////////////  linear function for Cooling
                    xa = 75	//outside temp a
                    ya = CSPSet // desired cooling temp a 

                    xb = 100 		//outside temp b
                    yb = CSPSet + 5  // desired cooling temp b  

                    // take humidity into account
                    // if outside humidity is higher than .... 
                    if(TooHumid){
                        xa = 75				//outside temp a LESS VARIATION WHEN HUMID
                        ya = CSPSet	   // desired cooling temp a 
                        xb = 100 //outside temp b
                        yb = CSPSet + 2 // desired cooling temp b  LESS VARIATION WHEN HUMID
                    }

                    coef = (yb-ya)/(xb-xa)

                    b = ya - coef * xa // solution to ya = coef*xa + b // CSPSet = coef*outsideTemp + b

                    CSPSet = coef*outsideTemp + b as double
                        log.info "b is: $b ---------------------------------------"
                    //
                    CSPSet.toInteger()

                } 

                else if(adjustments == "Yes, but use a logarithmic variation"){
                    // logarithmic treatment 

                    /* concept: x = log(72)75   to what power (that is to say "x") do I have to raise 72, to get to 75?

logb(n) = loge(n) / loge(b)
Where log can be a logarithm function in any base, n is the number and b is the base. For example, in Java this will find the base-2 logarithm of 256:

Math.log(256) / Math.log(2)
=> 8.0
*/
                    // log base is: CSPSet
                    def Base = CSPSet
                    /////////////////////////COOL//////////////////// 
                    //outsideTemp = 90 // for test only 
                    CSPSet = (Math.log(outsideTemp) / Math.log(Base)) * CSPSet
                    log.debug "Logarithmic CSPSet = $CSPSet"
                    //CSPSet = Math.round(CSPSet)
                    CSPSet = CSPSet.toInteger()
                    log.debug "Integer CSPSet = $CSPSet"

                    /////////////////////////HEAT//////////////////// 
                    /* // log base is: HSPSet
Base = 75
//outsideTemp = 60 // for test only 
HSPSet = (Math.log(outsideTemp) / Math.log(Base)) * HSPSet
log.debug "Logarithmic HSPSet = $HSPSet"
//CSPSet = Math.round(HSPSet)
HSPSet = HSPSet.toInteger()
log.debug "Integer HSPSet = $HSPSet"
*/

                    /////////////////////////HEAT//////////////////// ALWAYS linear function for heating

                    xa = 65	//outside temp a
                    ya = HSPSet // desired heating temp a 

                    xb = 45 		//outside temp b
                    yb = HSPSet + 5  // desired heating temp b  

                    coef = (yb-ya)/(xb-xa)
                    b = ya - coef * xa // solution to ya = coef*xa + b // HSPSet = coef*outsideTemp + b

                    log.info "b is: $b ---------------------------------------"
                    HSPSet = coef*outsideTemp + b as double


                        HSPSet = HSPSet.toInteger()
                    log.debug "linear HSPSet = $HSPSet"
                    // end of algebra        


                    if(useMotion && InMotionModes && AppMgt){

                        if(!Active){
                            // record algebraic CSPSet for debug purpose
                            def algebraicCSPSet = CSPSet 
                            def algebraicHSPSet = HSPSet
                            // log.info "$ThermSet default Cool: $CSPSet and default heat: $HSPSet "
                            CSPSet = CSPSet + CoolNoMotionVal  
                            HSPSet = HSPSet - HeatNoMotionVal

                            log.trace """
NO MOTION so $ThermSet CSP, which was $defaultCSPSet, then (if algebra) $algebraicCSPSet, is now set to $CSPSet
NO MOTION so $ThermSet HSP, which was $defaultHSPSet, then (if algebra) $algebraicHSPSet, is now set to $HSPSet
"""

                        }
                        else {

                            log.debug "There's motion in ${ThermSet}'s room (main loop)"
                        }
                    }

                    if(TooHumid && Inside - 2 >= outsideTemp && Active){
                        CSPSet = CSPSet - 1 
                        log.debug "Substracting 2 to new CSP because it is too humid OUTSIDE"
                    }
                    else {
                        log.debug "not too humid outside"
                    }

                    if(TooHumidINSIDE && Inside - 2 >= outsideTemp && Active){
                        CSPSet = CSPSet - 1 
                        log.debug "Substracting 1 to new CSP because it is too humid INSIDE"
                    }
                    else {
                        log.debug "not too humid inside"
                    }

                    // no lower than defaultCSPSet 
                    log.debug "Calculated CSPSet = $CSPSet, defaultCSPSet = $defaultCSPSet (loop $loopValue)"
                    if(CSPSet < (defaultCSPSet)){

                        log.info """CurrTemp at ${ThermSet} is: $CurrTemp. CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp
But, because CSPSet is too much lower than default value ($defaultCSPSet), default settings are maintained"""
                        CSPSet = defaultCSPSet
                    }
                    else {

                        log.info "CurrTemp at ${ThermSet} is: $CurrTemp  CSPSet was $defaultCSPSet. It is NOW $CSPSet due to outside's temperature being $outsideTemp"
                    }
                    if(HSPSet > (defaultHSPSet)){

                        log.info """CurrTemp at ${ThermSet} is: $CurrTemp. HSPSet was $defaultHSPSet. It is NOW $HSPSet due to outside's temperature being $outsideTemp
But, because HSPSet is too much higher than default value ($defaultHSPSet), default settings are maintained"""
                        HSPSet = defaultHSPSet
                    }
                    else {

                        log.info "CurrTemp at ${ThermSet} is: $CurrTemp  HSPSet was $defaultHSPSet. It is NOW $HSPSet due to outside's temperature being $outsideTemp"
                    }
                }
                /// END OF HSPSet and CSPSet eval
                state.CSPSet = CSPSet
                state.HSPSet = HSPSet
                // evaluate needs

                def WarmOutside = outsideTemp >= (CSPSet - 1)
                def WarmInside = CurrTemp - 1 > CSPSet
                log.debug "WarmOutside = $WarmOutside, WarmInside = $WarmInside"
                def ShouldCoolWithAC = WarmInside && WarmOutside

                log.debug "$ThermSet ShouldCoolWithAC = $ShouldCoolWithAC (before other criteria loop $loopValue)"

                def ShouldHeat = !WarmOutside && CurrTemp < HSPSet

                if((WarmInside && tooHumidINSIDE) && !ShouldHeat){
                    ShouldCoolWithAC = true
                    log.debug "ShouldCoolWithAC set to true loop $loopValue"
                }
                state.ShouldCoolWithAC = ShouldCoolWithAC
                state.ShouldHeat = ShouldHeat

                log.debug """
ShouldCoolWithAC = $ShouldCoolWithAC 
ShouldHeat = $ShouldHeat 
WarmOutside = $WarmOutside 
WarmInside = $WarmInside
OutsideTempLowThres = $OutsideTempLowThres

"""       

                def ThisIsExceptionTherm =  false

                if(NoTurnOffOnContact){
                    ThisIsExceptionTherm = "${ThermSet}" == "${NoTurnOffOnContact}"
                    log.debug "This is Exception Thermostat"

                }
                else {
                    ThisIsExceptionTherm =  false

                    log.debug "No exception contact selected by user, ThisIsExceptionTherm set to false by default"
                }

                if(ExceptionSW && ThermSet == "$ExceptionSwTherm" && SwitchesOn){
                    HSPSet = HSPSet + AddDegrees
                    CSPSet = CSPSet + SubDegrees

                    log.debug "$ThermSet SetPoints ExceptionSW active"                                
                }

                // Now, before sending any command, pull current setpoint and compare to avoid redundencies
                def CurrentCoolingSetPoint = ThermSet.currentValue("coolingSetpoint") 
                def CurrentHeatingSetPoint = ThermSet.currentValue("heatingSetpoint") 

                //// bedsensor/// 
                def CSPok = CurrentCoolingSetPoint == CSPSet
                def HSPok = CurrentHeatingSetPoint == HSPSet
                def BedSensorResults = BedSensorStatus()
                def NowBedisClosed = BedSensorResults[0]
                def NowBedisOpen = BedSensorResults[1]
                def BedSensorManagement = true


                def CurrentContactAndSwitch = ContactAndSwitch.currentSwitch
                log.debug "$ContactAndSwitch currentSwitch = $currentSwitch"
                def ContactAndSwitchState = CurrentContactAndSwitch.findAll { switchVal ->
                    switchVal == "on" ? true : false
                }


                if("${UnitToIgnore}" == "${ThermContact}" && "${ThermSet}" == "${ThermContact}" && ContactAndSwitchState.size() > O){
                    log.debug "not applying $BedSensor action because it is in the same room as $ContactAndSwitch, which is currently ON"
                }
                else if(KeepACon && ContactExceptionIsClosed){

                    if("${ThermSet}" == "${ThermContact}" && NowBedisClosed ){                      
                        BedSensorManagement = true 
                        log.debug """
NowBedisClosed = $NowBedisClosed, 
NowBedisOpen = $NowBedisOpen, """

                        log.debug "$BedSensor closed, applying settings accordingly"  
                        CSPSet = CSPSetBedSensor.toInteger()
                        HSPSet = HSPSetBedSensor.toInteger()
                        CSPok = CurrentCoolingSetPoint == CSPSet
                        HSPok = CurrentHeatingSetPoint == HSPSet
                        def needCool = ShouldCoolWithAC
                        def needHeat = ShouldHeat
                        if(needHeat){
                            ShouldCoolWithAC = false 
                        }

                        //state.AppMgtMap << ["$ThermSet" : "false"] 
                        state.AppMgtMap.remove("$ThermSet")
                        state.AppMgtMap["$ThermSet"] = false

                        if(needCool){
                            if(!CSPok){
                                ThermSet.setCoolingSetpoint(CSPSet)
                                log.debug "$ThermSet CSP set to $CSPSet -- Bed Sensor" 
                            }
                            if(ThermSet != "cool"){
                                ThermSet.setThermostatMode("cool") 
                                log.debug "$ThermSet set to cool -- Bed Sensor"
                            }      
                        }
                        else if(needHeat){
                            if(!HSPok){
                                ThermSet.setHeatingSetpoint(HSPSet)
                                log.debug "$ThermSet HSP set to $CSPSet -- Bed Sensor" 
                            }
                            if(ThermSet != "heat" ){
                                ThermSet.setThermostatMode("heat") 
                                log.debug "$ThermSet set to heat -- Bed Sensor"
                            }
                        }
                        log.trace """
BED SENSOR DEBUG

needHeat = $needHeat, 
needCool = $needCool, 

CurrTemp = $CurrTemp, 
CSPSetBedSensor = $CSPSetBedSensor
HSPSetBedSensor = $HSPSetBedSensor
CSPok = $CSPok
HSPok = $HSPok

"""
                    }

                    else {
                        BedSensorManagement = false
                    }
                }

                CSPok = CurrentCoolingSetPoint == CSPSet
                HSPok = CurrentHeatingSetPoint == HSPSet

                log.info "-- End of Temperatures Evals for $ThermSet" 




                log.debug """
HSPMap = $state.HSPMap
CSPMap = $state.CSPMap
InMotionModes?($InMotionModes)
useMotion?($useMotion)
Motion at $MotionSensor Active for the past $minutesMotion minutes?($Active)
FINAL CSPSet for $ThermSet = $CSPSet
ThisIsExceptionTherm is: $ThisIsExceptionTherm (${ThermSet} == ${NoTurnOffOnContact})
ContactExceptionIsClosed = $ContactExceptionIsClosed
Too Humid INSIDE?($TooHumidINSIDE : ${INSIDEhumidity}%)

Too Humid OUTSIDE?($TooHumid : $humidity)
ShouldCoolWithAC = $ShouldCoolWithAC (loop $loopValue), 
ShouldHeat = $ShouldHeat
Current setpoint for $ThermSet is $CurrentCoolingSetPoint, 
Current Heating setpoint is $CurrentHeatingSetPoint,
Final CSPSet is $CSPSet
Current Set Points for $ThermSet are: cooling: $CurrentCoolingSetPoint, heating: $CurrentHeatingSetPoint 
"""


                /////////////////////////END OF SP MODIFICATIONS//////////////////////////
                /////////////////////////SENDING COMMANDS//////////////////////////

                // for false overrides prevention

                state.HSPMap << ["$ThermSet": CSPSet]
                state.CSPMap << ["$ThermSet": HSPSet]
                state.HSPMap.sort()*.key
                state.CSPMap.sort()*.key

                log.debug "Maps updated"

                state.nextLoopResetsMaps = false
                if(state.HSPMap.size() >= Thermostats.size()){

                    state.nextLoopResetsMaps = true

                }

                log.debug "doorsOk = $doorsOk, CSPok = $CSPok, HSPok = $HSPok, $ThermSet; AppMgt = $AppMgt"

                if("${UnitToIgnore}" == "${ThermSet}" && ContactAndSwitchState.size() > O){
                    log.debug "not applying settings for $ThermSet because it is in the same room as $ContactAndSwitch, which is currently ON"
                }
                else {

                    if(doorsOk || (ContactExceptionIsClosed && ThisIsExceptionTherm)){

                        def inAutoOrOff = ThermState in ["auto","off"]
                        log.debug "turnOffWhenReached = $turnOffWhenReached, $ThermSet is inAutoOrOff = $inAutoOrOff, BedSenorManagement= $BedSenorManagement"


                        if(!BedSenorManagement){ /// avoid redundancies if BedSensor's already managing unit. 

                            if(!ShouldCoolWithAC && !ShouldHeat && turnOffWhenReached){

                                if(AltSensor && (!turnOffWhenReached || turnOffWhenReached)){ 

                                    if(!inAutoOrOff){

                                        state.LatestThermostatMode = "off"   
                                        // that's a "should be" value used to compare eventual manual setting to what "should be"
                                        // that's why it must be recoreded even during override mode
                                        //state.AppMgtMap[loopValue] = true //override test value
                                        if(AppMgt){
                                            log.debug "$ThermSet TURNED OFF"  
                                            ThermSet.setThermostatMode("off") 
                                        }
                                        else {
                                            log.debug "$ThermSet in OVERRIDE MODE, Setting it to AUTO"
                                            if(ThermSet != "auto"){
                                                ThermSet.setThermostatMode("auto") 
                                            }
                                        }
                                    }
                                    else {
                                        log.debug "$ThermSet already set to off"
                                    }
                                }
                                else if(turnOffWhenReached && !AltSensor){
                                    if(!inAutoOrOff){
                                        state.LatestThermostatMode = "off"                
                                        if(AppMgt){
                                            log.debug "$ThermSet TURNED OFF" 
                                            ThermSet.setThermostatMode("off")   
                                        }
                                        else {
                                            log.debug "$ThermSet in OVERRIDE MODE, doing nothing"
                                        }
                                    }
                                    else {
                                        log.debug "$ThermSet already set to off"
                                    }
                                }
                            }

                            else if(ShouldCoolWithAC || !CSPok){
                                // it may happen that old settings get stuck if estimate of shouldcool is false 
                                // so if no override but discrepancy between current csp and what should be
                                // go on
                                log.debug """ShouldCoolWithAC EVAL $loopValue AppMgt = $AppMgt
CurrentCoolingSetPoint == CSPSet ? ${CurrentCoolingSetPoint == CSPSet}"""

                                state.LatestThermostatMode = "cool"
                                if(AppMgt){
                                    log.debug " $CurrentCoolingSetPoint == $CSPSet {CurrentCoolingSetPoint == CSPSet}?"
                                    if(!CSPok){
                                        ThermSet.setCoolingSetpoint(CSPSet)
                                        log.debug "$ThermSet CSP set to $CSPSet" 
                                    }
                                    else{
                                        log.debug "Cooling SetPoint already set to $CSPSet for $ThermSet ($CSPSet == $CurrentCoolingSetPoint)"
                                    }                   
                                    if(ShouldCoolWithAC && ThermState != "cool"){  
                                        // ShouldCoolWithAC has to be rechecked here otherwise !CSPok might trigger heat while no need
                                        log.debug "$ThermSet set to cool"
                                        ThermSet.setThermostatMode("cool") 
                                    }
                                    else {
                                        if(!ShouldCoolWithAC){
                                            log.debug "no need to cool at $ThermSet"
                                        }
                                        else{
                                            log.debug "$ThermSet already set to cool"
                                        }
                                    }
                                }
                                else {
                                    log.debug "$ThermSet in OVERRIDE MODE, doing nothing"
                                }
                            }
                            else if(ShouldHeat || !HSPok){
                                state.LatestThermostatMode = "heat"
                                if(AppMgt){
                                    //state.AppMgtMap[loopValue] = true
                                    if(CurrentHeatingSetPoint != HSPSet){
                                        //state.AppMgtMap[loopValue] = true //override test value
                                        ThermSet.setHeatingSetpoint(HSPSet)
                                        log.debug "$ThermSet HSP set to $HSPSet" 
                                    }
                                    else { 
                                        log.debug "Heating SetPoint already set to $HSPSet for $ThermSet"
                                    }

                                    if(ShouldHeat && ThermState != "heat"){
                                        // ShouldHeat has to be rechecked here otherwise !HSPok might trigger heat while no need

                                        log.debug "$ThermSet set to Heat"
                                        ThermSet.setThermostatMode("heat")  
                                    }
                                    else {
                                        if(!ShouldHeat){
                                            log.debug "no need to heat at $ThermSet"
                                        }
                                        else{
                                            log.debug "$ThermSet already set to heat"
                                        }
                                    }
                                }
                                else {
                                    log.debug "$ThermSet in OVERRIDE MODE, doing nothing"
                                }
                            } 
                        }
                        else {
                            log.debug "$ThermSet mangaged by $BedSensor status, skipping"
                        }
                    }
                    else {
                        log.debug "Not evaluating for $ThermSet because some windows are open"
                        // check that therms are off  

                        def AnyON = Thermostats.findAll{ it?.currentValue("thermostatMode") != "off"}
                        log.debug "there are ${AnyON.size()} untis that are still running: $AnyON"

                        def count = 0

                        for(count = 0; count < Thermostats.size(); count++){ 

                            def device = AnyON[count]
                            if(ThermState != "off"){
                                if(device != null && !ThisIsExceptionTherm){
                                    device.setThermostatMode("off") 
                                    log.debug "$device TURNED OFF BECAUSE SOME CONTACTS ARE OPEN"
                                }
                                if(ThisIsExceptionTherm && !ContactExceptionIsClosed){
                                    device.setThermostatMode("off") 
                                    log.debug "$device TURNED OFF BECAUSE EXCEPTION CONTACT IS OPEN"
                                }
                                if(ThisIsExceptionTherm && !InExceptionContactMode){
                                    device.setThermostatMode("off") 
                                    log.debug "$device TURNED OFF BECAUSE this is not one of the exception modes"
                                }
                            }
                            else {
                                log.debug "device already off"
                            }
                        }
                    }
                }


                log.info """
INFO : 
$ThermSet CSP should be : $CSPSet current CSP: $CurrentCoolingSetPoint
$ThermSet HSP should be : $HSPSet current HSP: $CurrentHeatingSetPoint
"""
                log.trace " END OF FOR LOOP $loopValue" 
            }   
            // true end of  loop
        }
    else { 
        log.debug "not evaluating because some windows are open" 
        TurnOffThermostats()
        Thermostats.setThermostatMode("off") // temporary because of those idiots at smartthings who pushed a fucking stupid useless update that prevents status refresh
        state.thisIsWindowMgt = false
    }


    EndEvalTRUE()
    /// disabled FOR TESTS
    //OverrideReset()
}
def  UpdateMaps(){

}
def IndexValueMode(){
    def ModeInArray = WhichMode()

    def ModeList = ["$Home", "$Night", "$Away", "$CustomMode1", "$CustomMode2"]  
    def LetterModeList = ["H", "N", "A", "Cust1_T", "Cust2_T"]
    def NumberModeList = ["0", "1", "2", "3", "4"]

    def ModeMapList = [:]
    def lv = 0
    def size = LetterModeList.size()
    def ModeValue = null
    def ModeFound = null
    def ModeMatches = false
    def ModeIndexValue = null
    for(size > 0; lv < size; lv++){
        /// DO NOT DELETE THIS EXAMPLE! (ModeMapList) it will be usefull for further A.I. developments 
        /// ModeMapList << [(ModeList[lv]) : (LetterModeList[lv])]    
        /// build a map of all modes and set a Srting key for each mode
        /// this allows for writing a new variable below
        ModeFound = ModeList[lv] // each mode in the array created above, is an array in itself

        ModeMatches = ModeFound.contains("$ModeInArray") // so find to which array mode the current mode belongs to

        log.debug "ModeFound is : $ModeFound || ModeMatches = $ModeMatches || ModeInArray = $ModeInArray"
        if(ModeMatches){
            log.debug "MATCH!"
            ModeValue = "${LetterModeList[lv]}" // attribute mode letter to start writing the new variable
            ModeIndexValue = "${NumberModeList[lv]}" 
            // break this loop so it doesn't apply a match to other modes (since now ModeMatches = true)
            break
        }

        log.debug "mode found = $ModeValue && $ModeIndexValue"
    }

    log.debug """ModeValue = $ModeValue && ModeIndexValue = $ModeIndexValue"""

    return [ModeValue, ModeIndexValue]
}

def WhichMode(){
    def CurrMode = location.currentMode
    def ModeInArray = null
    if(CurrMode in CustomMode1){
        ModeInArray = CustomMode1.find{it == "$CurrMode"}
        log.debug "ARRAY MODE IS: $ModeInArray"
    }
    else if(CurrMode in CustomMode2){
        ModeInArray = CustomMode2.find{it == "$CurrMode"}
        log.debug "ARRAY MODE IS: $ModeInArray"
    }
    else if(CurrMode in Home){
        ModeInArray = Home.find{it == "$CurrMode"}
        log.debug "ARRAY MODE IS: $ModeInArray"
    }
    else if(CurrMode in Night){
        ModeInArray = Night.find{it == "$CurrMode"}
        log.debug "ARRAY MODE IS: $ModeInArray"
    }
    return ModeInArray
}

def EndEvalTRUE(){
    state.EndEval = true
    log.info "state.EndEval = $state.EndEval"
}
def EndEvalFALSE(){
    state.EndEval = false
    log.info "state.EndEval = $state.EndEval"
}
def DoNotTurnBackOnMode(){
    def result = false
    def inMode = location.currentMode in SwitchMode

    if(ToggleBack || SwitchIfMode){
        if(SwitchIfMode){
            if(inMode){
                result = true
            }
            else {
                result = false 
            }
        }
        else {
            result = false 
        }
    }
    else {
        result = false 
    }
    log.debug "DoNotTurnBackOnMode = $result"
    return result 
}
//shoulds
def LatestThermMode(){
    // recording currently set values for when users want to end an override
    // by setting a unit back to its current default's values 
    // first, it happens that no command was ever sent since last updated()
    // or due to contacts sensors or due to previous overrides
    // so let's reuse the same conditions, 
    // but without the consecutive actions/commands
    // nor the state comparisons which are meant only to avoid redundent z-wave commands
    // so we can record just the ShouldBe values (i.e. what should be if there were no override)
    // which are used by ThermostatSwitchHandler and setpointHandler for override assertion

    def ShouldCoolWithAC = state.ShouldCoolWithAC
    def ShouldHeat = state.ShouldHeat
    def LatestThermMode = "null"
    def ThermSet = state.ThermSet

    if(!ShouldCoolWithAC && !ShouldHeat){ 
        if(!AltSensor){
            LatestThermMode = "off"             
        } 
        else {
            log.debug "$ThermSet managed by $CurrTempDevice, so it won't be recorded as ShouldBe = off"
        }
    }

    else if(ShouldCoolWithAC){                    
        LatestThermMode = "cool"                            
    }
    else if(ShouldHeat){
        LatestThermMode = "heat"
    }

    return LatestThermMode
}

// A.I. and micro location evt management
def motionSensorHandler(evt){
    log.debug "motion is $evt.value at $evt.device"
    if(state.EndEval == true){
        if(evt.value == "active"){ 
            Evaluate()
        }
    }
    else {
        log.debug "Evaluate() is busy"
    }
}
def HumidityHandler(evt){

    log.info "humidity value is ${evt?.value}%"
    state.humidity = evt.value

}
def InsideHumidityHandler(evt){
    log.info "INSIDE humidity value at $evt.device is ${evt.value}%"
    state.INSIDEhumidity = evt.value
}
def WindHandler(evt){

    log.info "Wind value is ${evt.value}mph"  

    state.wind = evt.value

}
def FeelsLikeHandler(evt){

    log.info "Currently, outside temperature feels like ${evt.value}F"  

    state.FeelsLike = evt.value

}

def switchHandler(evt){

    log.debug "switchHandler : ${evt.device} is ${evt.value}"

    if(ExceptionSW && evt.value == "on"){
        state.exception = true
    } else {
        state.exception = false
    }

}
def ContactAndSwitchHandler(evt){
    log.debug "ContactAndSwitchHandler : ${evt.device} is ${evt.value}"

    if(evt.value == "off"){
        state.contactAndSwtchOff = true
    } else {
        state.contactAndSwtchOff = false
    }

}
def BedSensorHandler(evt){

    log.debug """$evt.device is $evt.value 
BedSensor is $BedSensor------------------------------------------------------------------------"""


    if(state.EndEval == true){
        Evaluate()
    }
    else {
        log.debug "Evaluate() is busy"
    }

}
def Timer() {
    def minutes = findFalseAlarmThreshold() 
    def deltaMinutes = minutes * 60000 as Long

    def start = new Date(now() - deltaMinutes)
    def end = new Date(now())
    def ContactsEvents = BedSensor?.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
    //BedSensor[0].statesBetween("contact", start, end, [max: 200]))

    log.debug """
Found ${ContactsEvents.size()} events in the last $minutes minutes"
"""
    def size = ContactsEvents.size()
    return size

}
private findFalseAlarmThreshold() {
    // In Groovy, the return statement is implied, and not required.
    // We check to see if the variable we set in the preferences
    // is defined and non-empty, and if it is, return it.  Otherwise,
    // return our default value of 10
    (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold : 2
}
def BedSensorStatus(){
    def ConsideredOpen = true // has to be true by default in case no contacts selected
    def BedSensorAreClosed = false // has to be false by default in case no contacts selected
    if(BedSensor){

        def CurrentContacts = BedSensor.currentValue("contact")    
        def ClosedContacts = CurrentContacts.findAll { val ->
            val == "closed" ? true : false}

        if(ClosedContacts.size() == BedSensor.size()){
            BedSensorAreClosed = true
        }

        log.debug "${ClosedContacts.size()} sensors out of ${BedSensor.size()} are closed SO BedSensorAreClosed = $BedSensorAreClosed"
        def ContactsEventsSize = Timer()

        def Open = BedSensor.findAll{it.currentValue("contact") == "open"}

        boolean isOpen = Open.size() != 0 && !BedSensorAreClosed
        log.debug "Open = ${Open}, isOpen = $isOpen"

        if(isOpen && ContactsEventsSize > 1){
            ConsideredOpen = false
            log.debug "too many events in the last couple minutes"
        }
        else if (isOpen && ContactsEventsSize == 1){  

            def Map = [:]
            def i = Thermostats.size()
            def loopV = 0
            def Therm = null
            for(i != 0; loopV < i; loopV++){
                Therm = Thermostats[loopV]
                log.info "Therm is $Therm"
                Map << ["$Therm": loopV]
            }

            log.debug "Map = $Map"
            //["${Thermostats[0]}": "0" , "${Thermostats[1]}": 1, "${Thermostats[2]}": "2", "${Thermostats[3]}": "3"]

            def KeyValueForThisTherm = Map.find { it.key == "$ThermContact"}
            log.info "device is ------------------- $KeyValueForThisTherm.value"
            def ThermNumber = KeyValueForThisTherm.value
            ThermNumber = KeyValueForThisTherm.value.toInteger()


            state.AppMgtMap.remove("$ThermContact")
            state.AppMgtMap["$ThermContact"] = false 

            ConsideredOpen = true
            log.debug "Only one event within the last couple minutes"

        }

    }


    log.debug "BedSensorAreClosed = $BedSensorAreClosed, ConsideredOpen = $ConsideredOpen"
    return [BedSensorAreClosed, ConsideredOpen]

}

// Main events management

def temperatureHandler(evt) {

    handlerrunningTRUE()

    def doorsOk = AllContactsAreClosed()

    if(evt.device == XtraTempSensor) {
        state.Inside = evt.value

    }

    def currentTemp = state.Inside
    log.info """
current temperature value for $evt.device is $evt.value
Xtra Sensor (for critical temp) is $XtraTempSensor and its current value is $currentTemp and CriticalTemp is $CriticalTemp
"""

    if(currentTemp <= CriticalTemp) {
        log.info "EMERGENCY HEATING - TEMPERATURE IS TOO LOW!" 

        Thermostats.setThermostatMode("heat") 

        state.CRITICAL = true

        def message = ""
        if(Actuators && !doorsOk){
            log.debug "CHECKING IF WINDOWS SHOULD BE CLOSED "
            if(state.windowswereopenandclosedalready == false){

                message = "Closing windows because $state.causeClosed"
                send(message)
                Actuators?.off()
                ActuatorException?.off()
                // allow for user to reopen them if they want to. 
                state.windowswereopenandclosedalready = true // windows won't close again as Long as temperature is still critical to allow for user's override 
                // this value must not be reset by updated() because updated() is run by contacthandler it is only reset here or after new installation of the app
            }
            else { 
                message = "doors and windows already reopened by user so not running emergency closing. BEWARE! these windows will not close again"
                log.info message
                send(message)

            }
        } 
        state.TheresBeenCriticalEvent = true
    } 
    else 
    { 
        log.debug "CriticalTemp OK"
        state.CRITICAL = false
        state.windowswereopenandclosedalready = false

        state.TheresBeenCriticalEvent = false
    } 

    handlerrunningFALSE()

    if(state.EndEval == true){
        Evaluate()
    }
    else {
        log.debug "Evaluate() is busy"
    }
}
def contactHandlerClosed(evt) {

    state.thisIsWindowMgt = false 
    state.attempts = 0 // for future reset of thisiswindowsmgt()

    def message = ""

    log.debug "$evt.device is $evt.value" 

    log.info "List of devices' status is $CurrentContactsState"


    if(!AllContactsAreClosed()){
        log.debug "Not all contacts are closed, doing nothing"

    }
    else {      
        log.debug "all contacts are closed, unscheduling previous TurnOffThermostats command"
        unschedule(TurnOffThermostats) // in case were closed within time frame

        //log.debug "state.ClosedByApp = $state.ClosedByApp"

        if(state.ClosedByApp == false && state.OpenByApp == true && evt.value == "closed"){ 

            message = "Windows $evt.value manualy and they will not open again until you open them yourself"
            log.info message
            send(message)


        }
    }

    if(AllContactsAreClosed()){
        updated()
    }

} 
def contactHandlerOpen(evt) {
    log.debug "$evt.device is now $evt.value, Turning off all thermostats in $TimeBeforeClosing seconds"

    state.attempts = 0 // reset of thisiswindowsmgt()
    state.thisIsWindowMgt = true // prevent false ON/OFF override

    runIn(TimeBeforeClosing, TurnOffThermostats)   
    def message = ""

    //log.debug "state.OpenByApp = $state.OpenByApp"
    if(state.OpenByApp == false && state.ClosedByApp == true && evt.value == "open"){ 

        message = "Windows $evt.value manualy and will not close again until you close them yourself"
        log.info message
        send(message)

    }

    runIn(60, thisIsWindowMgtFALSE)
}
def contactExceptionHandlerOpen(evt) {
    handlerrunningTRUE()
    state.thisIsWindowMgt = true

    state.ThermOff = false
    //log.debug "$evt.device is now $evt.value (Contact Exception), Turning off all thermostats in $TimeBeforeClosing seconds"

    if(OperatingTime){
        runIn(TimeBeforeClosing, TurnOffThermostats)  
    }
    else{
        TurnOffThermostats()
    }
    handlerrunningFALSE()
    runIn(5, thisIsWindowMgtFALSE)
}
def contactExceptionHandlerClosed(evt) {
    state.ThermOff = false
    log.debug "$evt.device is now $evt.value (Contact Exception), Resuming Evaluation for $NoTurnOffOnContact"


    //AppMgtTrue()

    if(state.EndEval == true){
        Evaluate()
    }
    else {
        log.debug "Evaluate() is busy"
    }
}
def ChangedModeHandler(evt) {

    //log.debug "mode changed to ${evt.value}"
    def ContactsClosed = MainContactsClosed()

    if(ContactsClosed) {
        // windows are closed 
        state.ClosedByApp = true // app will open windows if needed 
        state.OpenByApp = false 
        // has to be the default value so it doesn't close again if user opens windows manually or another app. 
        // Beware that this can be a serious safety concern (for example, if a user has these windows linked to a smoke detector
        // so do not modify these parameters under any circumstances 
        // and check that this works after any modification you'd bring to this app

        state.ThermOff = false

    } 

    state.recentModeChange = true
    runIn(60, recentModeChangeFALSE)
    OverrideReset()
    updated()

}
def recentModeChangeFALSE(){
    state.recentModeChange = false
}

//override management
def handlerrunningTRUE(){
    state.handlerrunning = true
    log.info "handlerrunning($state.handlerrunning)"
}
def handlerrunningFALSE(){
    state.handlerrunning = false
    log.info "handlerrunning($state.handlerrunning)"
}
def setpointHandler(evt){
    handlerrunningTRUE()

    log.trace "${evt.device}'s $evt.name set to $evt.value (setpointHandler)"

    def Endeval = state.EndEval
    if(state.HSPMap.size() > Thermostats.size() || !Endeval){

        log.debug "Skipping setpoint override check"
        // "state" is modified only as of the moment Evaluate() is done so we need to wait before last sent command has been recorded as such to avoid false overrides
    }
    else {
        // declare an integer value for the thermostat which has had its values modified

        def tsize = Thermostats.size()
        def loopV = 0
        def MapModesThermostats = [:]
        for(tsize != 0; loopV < tsize; loopV++){
            MapModesThermostats << ["${Thermostats[loopV]}": "$loopV"]
        }
        log.debug "MapModesThermostats = $MapModesThermostats"

        def KeyValueForThisTherm = MapModesThermostats.find { it.key == "$evt.device"}
        log.info "device is ------------------- $KeyValueForThisTherm.key"
        def ThermNumber = KeyValueForThisTherm.value
        ThermNumber = KeyValueForThisTherm.value.toInteger()

        log.info "ThermNumber is ------------------- $ThermNumber"

        //def AppMgtList = state.AppMgtMap
        // def AppMgt = AppMgtList.find {it.value == "$evt.device"}

        log.trace """AppMgt at SetpointHandler for $ThermNumber ($KeyValueForThisTherm.key) is $AppMgt

"""
        def CurrMode = location.currentMode

        def HomeMode = null

        def reference = null
        def termRef = null
        def AltSENSOR = false    

        log.info "Home modes are : $Home, Night modes are : $Night, Away mode is : $Away, CustomMode1 are : $CustomMode1, CustomMode2 are : $CustomMode2"

        //log.debug "CurrMode is $CurrMode mode"
        //array heat

        def ModeValueList = IndexValueMode()
        def ModeIndexValue = ModeValueList[1].toInteger()

        //array cool
        // example: 
        //HSPMap = [Temperature LIVING:72, Temperature BEDROOM:72, Temperature LIVING:72, NewHSP2:72, NewHSP1:72, Temperature OFFICE:72, Temperature OFFICE:72, Temperature BEDROOM:72, NewHSP0:72]
        //CSPMap = [Temperature LIVING:72, NewCSP0:68, NewCSP1:72, Temperature BEDROOM:68, Temperature LIVING:72, NewCSP2:72, Temperature OFFICE:72, Temperature OFFICE:72, Temperature BEDROOM:68]
        def HSPMap = state.HSPMap
        def CurrentHSP = HSPMap.find{it.key == "$evt.device"}
        CurrentHSP = CurrentHSP.value.toInteger()

        log.debug "CurrentHSP = $CurrentHSP"
        def RefHeat = CurrentHSP
        log.debug "RefHeat = $RefHeat"

        //do the same with cooling values
        def CSPMap = state.CSPMap
        def CurrentCSP = CSPMap.find{it.key == "$evt.device"}
        CurrentCSP = CurrentCSP.value.toInteger()

        log.debug "CurrentCSP = $CurrentCSP"
        def RefCool = CurrentCSP
        log.debug "RefCool = $RefCool"


        // for thermostat set to work based on alternate temp sensors, the Alternativesensor() loops will 
        // simply stop running after this new setting has been compared to settings() in the arrays above
        // by declaring the state.AppMgnt_T_X variable as false.  

        if(evt.name == "heatingSetpoint"){              
            reference = RefHeat

            log.trace """RefHeat is $RefHeat and it is now converted to a reference for comparison"""

        }
        else  if(evt.name == "coolingSetpoint"){ 
            reference = RefCool

            log.debug "RefCool is $RefCool and it is now converted to a reference for comparison"
        }


        def ThisIsModeChange = state.locationModeChange
        def ExceptionState = state.exception
        def thisIsExceptionTemp = evt.displayName == "$NoTurnOffOnContact" && ExceptionState



        def Value = evt.value
        //def Value = Math.round(Double.parseDouble(evt.value))
        Value = Value.toInteger()
        reference = reference.toInteger()
        //log.debug "Evt value to Integer is : $Value and it is to be compared to reference: $reference"
        def ValueIsReference = true
        if (Value != reference){
            ValueIsReference = false
        }
        def doorsOk = AllContactsAreClosed()

        if(ValueIsReference || ThisIsModeChange ||  (!doorsOk && !thisIsExceptionTemp))
        {  
            log.debug "NO SETPOINT OVERRIDE for $evt.device"


            state.AppMgtMap.remove("$evt.device")
            state.AppMgtMap["$evt.device"] = true // restore normal operation 
        }
        else {
            log.info "OVERRIDES ARE NOT WORKING AT THE MOMENT"
            //log.info "MANUAL SETPOINT OVERRIDE for $evt.device"
            /// NOT UNTIL IT WORKS... 

            state.AppMgtMap.remove("$evt.device")
            state.AppMgtMap["$evt.device"] = false 

        }

        log.trace """
state.AppMgtMap = $state.AppMgtMap
HSPMap = $state.HSPMap
CSPMap = $state.CSPMap
Possible causes: 

ValueIsReference = $ValueIsReference (should be false if override)
ThisIsModeChange = $ThisIsModeChange (should be false if override)
doorsOk should be true if override: $doorsOk
thiIsExceptionTemp : $thisIsExceptionTemp
RefHeat for $evt.device is: $RefHeat 
RefCool for $evt.device is: $RefCool 
reference for $evt.device is: $reference
ThisIsModeChange : $ThisIsModeChange
ValueIsReference: $ValueIsReference [Reference = $reference && evt.value = $evt.value]
doorsOk = $doorsOk
OVERRIDE? if true then should have $reference != $Value 
(unless location mode just changed or Exception Switch is on or ThisIsMotion or ThisIsLinearEq)
"""



    }
    handlerrunningFALSE()
}
def ThermostatSwitchHandler(evt){

    handlerrunningTRUE()

    def Endeval = state.EndEval
    /* while(Endeval != true)
{
Endeval = state.EndEval
log.debug "waiting"
}*/

    log.trace """$evt.device set to $evt.value (ThermostatSwitchHandler)
"""
    // do be done // /// ////////////////////////////////////////////////////////////////////////////// ON/OFF OVERRIDES

    if(state.CRITICAL == false){
        def CurrMode = location.currentMode
        def LocatioModeChange = state.locationModeChange
        //state.thisIsWindowMgt = false
        def thisIsWindowMgt = state.thisIsWindowMgt
        def ExceptionState = state.exception
        def thisIsExceptionTemp = evt.displayName == "${Thermostats[0]}" && ExceptionState
        //log.debug "Location Mode Changed?($LocatioModeChange)"

    }
    else { 
        log.debug "CRITICAL MODE. NOT EVALUATING OVERRIDES" 
    }

    handlerrunningFALSE()

}
def thisIsWindowMgtFALSE(){
    log.debug "state.thisIsWindowMgt = $state.thisIsWindowMgt"
    state.attempts = state.attempts + 1
    if(state.handlerrunning == false){
        state.thisIsWindowMgt = false
        log.debug "Reset state.thisIsWindowMgt to FALSE "        
    }
    else if(state.attempts >= 3){
        state.thisIsWindowMgt = false
        log.debug "Reset state.thisIsWindowMgt to FALSE " 
    }

    else {
        log.debug "Handler is running, trying reset of thisIsWindowMgt in 15 seconds"
        state.attempts = state.attempts + 1
        runIn(15, thisIsWindowMgtFALSE)
    }
}
def OverrideReset(){
    def t = Thermostats.size()
    def i = 0
    state.AppMgtMap = [:]
    def Therm = null
    for(t != 0; i < t; i++){
        Therm = Thermostats[i]
        state.AppMgtMap << ["$Therm" : true]
    }
    log.info "OVERRIDES RESET: $state.AppMgtMap"
}

def resetLocationChangeVariable(){
    state.locationModeChange = false
    //log.debug "state.locationModeChange reset to FALSE"
}

// contacts and windows management and motion bool tests
def MainContactsClosed(){

    def MainContactsAreClosed = true // has to be true by default in case no contacts selected


    def CurrentContacts = Maincontacts.currentValue("contact")

    log.debug """Maincontacts are $Maincontacts
CurrentContacts States = $CurrentContacts"""

    def ClosedContacts = CurrentContacts.findAll { AllcontactsAreClosed ->
        AllcontactsAreClosed == "closed" ? true : false}

    MainContactsAreClosed = ClosedContacts.size() == Maincontacts.size() 

    log.debug "${ClosedContacts.size()} windows/doors out of ${Maincontacts.size()} are closed SO MainContactsAreClosed = $MainContactsAreClosed"

    return MainContactsAreClosed
}
def ExcepContactsClosed(){

    def ContactsExepClosed = true
    if(ContactException){
        def CurrentContactsExept = ContactException.currentValue("contact")    
        def ClosedContactsExpt = CurrentContactsExept.findAll { AllcontactsExeptAreClosed ->
            AllcontactsExeptAreClosed == "closed" ? true : false
        }
        ContactsExepClosed = ClosedContactsExpt.size() == ContactException.size() 
        log.debug "${ClosedContactsExpt.size()} windows/doors out of ${ContactException.size()} are closed SO ContactsExepClosed = $ContactsExepClosed"

        def NoTurnOffOnContact = Thermostats.find {NoTurnOffOnContact << it.device}        

        log.debug "NoTurnOffOnContact = $NoTurnOffOnContact --------------"

        def CurrTherMode = NoTurnOffOnContact.currentValue("thermostatMode")
        // //log.debug "Current Mode for $Thermostat_1 is $CurrTherMode"
        if(CurrTherMode != "off" && !ContactsExepClosed){
            // //log.debug "$Thermostat_1 is on, should be off. Turning it off" 
            NoTurnOffOnContact.setThermostatMode("off") 
            state.LatestThermostatMode_T1 = "off"
        }
    }
    return ContactsExepClosed

}
def AllContactsAreClosed() {

    def AllContactsClosed = MainContactsClosed() && ExcepContactsClosed()
    log.debug "AllContactsAreClosed() $AllContactsClosed"

    return AllContactsClosed
}
def AllContactsAreOpen() {

    def MainContactsAreAllOpen = false // has to be true by default in case no contacts selected

    def CurrentContacts = Maincontacts.currentValue("contact")    
    def OpenContacts = CurrentContacts.findAll { AllcontactsAreOpen ->
        AllcontactsAreOpen == "open" ? true : false}
    log.debug "${OpenContacts.size()} windows/doors out of ${Maincontacts.size()} are open"
    def OpenMainSize = OpenContacts.size()
    def MainSize = Maincontacts.size() 
    MainContactsAreAllOpen = OpenMainSize == MainSize


    def AllOpen = MainContactsAreAllOpen
    ///
    if(ContactException){
        def ContactsExepOpen = false

        def CurrentContactsExept = ContactException.currentValue("contact")    
        def OpenContactsExpt = CurrentContactsExept.findAll { AllcontactsExeptAreOpen ->
            AllcontactsExeptAreOpen == "open" ? true : false
        }
        //  //log.debug "${OpenContactsExpt.size()} windows/doors out of ${ContactException.size()} are open SO ContactsExepOpen = $ContactsExepOpen"

        def OpenExptSize = OpenContactsExpt.size()
        def ExceptionSize = ContactException.size() 
        ContactsExepOpen = OpenExptSize == ExceptionSize

        AllOpen = ContactsExepOpen && MainContactsAreAllOpen

    }
    //log.debug "AllOpen?($AllOpen)"

    return AllOpen  

}

def TurnOffThermostats(){

    def InExceptionContactMode = location.currentMode in DoNotTurnOffModes
    log.debug "InExceptionContactMode = $InExceptionContactMode  DoNotTurnOffModes = $DoNotTurnOffModes (TurnOffThermostats)"

    if(ContactAndSwitch){
        def contactClosed = false

        if(ContactException && FollowException && InExceptionContactMode){

            contactClosed = ExcepContactsClosed()
        }
        else{
            contactClosed =  AllContactsAreClosed()
        }
        log.debug "contactClosed = $contactClosed (TurnOffThermostats)"
        def CurrentSwitch = ContactAndSwitch.currentSwitch
        log.debug "$ContactAndSwitch currentSwitch = $currentSwitch"
        def SwState = CurrentSwitch.findAll { switchVal ->
            switchVal == "on" ? true : false
        }
        log.trace "SwState = $SwState"
        if(!contactClosed){
            if(SwState.size() != 0){
                ContactAndSwitch?.off()
                log.debug "$ContactAndSwitch TURNED OFF"
                //state.turnedOffByApp == true
            }
            else {
                log.debug "$ContactAndSwitch already off"
            }
        }
    }

    def loopValue = 0
    def t = Thermostats.size()


    def doorsOk = AllContactsAreClosed()

    def ContactExceptionIsClosed = ExcepContactsClosed()

    if(state.CRITICAL == false){
        for(t > 0; loopValue < t; loopValue++){

            def ThermSet =  Thermostats[loopValue]
            def ThermState = ThermSet.currentValue("thermostatMode")   
            log.debug "${ThermSet}'s ThermState = $ThermState"


            log.trace "Turning off thermostats: ContactExceptionIsClosed: $ContactExceptionIsClosed, InExceptionContactMode: $InExceptionContactMode, NoTurnOffOnContact: $NoTurnOffOnContact"

            if((!NoTurnOffOnContact || !InExceptionContactMode || !ContactExceptionIsClosed) && "${ThermSet}" == "${NoTurnOffOnContact}"){
                if(ThermState != "off"){
                    // to avoid false end of override while windows are open and exception thermostat still needs to remain in override mode. 

                    ThermSet.setThermostatMode("off") 
                    state.LatestThermostatMode = "off"
                    log.debug "$ThermSet  turned off"

                }
            }
            else {

                // log.debug "Not turning off $ThermSet because current mode is within exception modes selected by the user"

                state.LatestThermostatMode = ThermState
            }

            if("${ThermSet}" != "${NoTurnOffOnContact}"){
                if(ThermState != "off"){  

                    ThermSet.setThermostatMode("off") 
                    state.LatestThermostatMode = "off"
                    log.debug "$ThermSet turned off"

                }
                else {
                    log.debug "$ThermSet ALREADY off"
                }
            }
        }
        state.ThermOff = true
    }

    else { 
        log.debug "CRITICAL MODE, NOT TURNING OFF ANYTHING" 


    }


}
def MotionTest(){

    def loopV = 0

    def s = MotionSensor.size()



    //def s = MotionSensor.findAll{it.device != null}.sort()




    loopV = 0
    def o = null
    def i = 0
    def x = []
    def ThisSensorList = []

    for(a > 0; i < s; i++){

    }

    log.debug "state.SensorThermMap = $state.SensorThermMap"

    i = 0
    def deltaMinutes = minutesMotion * 60000 as Long

    def motionEvents = []
    def Active = []
    for(s > 0; i < s; i++){
        motionEvents[i] = MotionSensor[i].collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
        Active[i] = motionEvents[i].size() != 0
        log.debug """
Found ${motionEvents[i].size() ?: 0} events in the last $minutesMotion minutes at ${MotionSensor[i]}
deltaMinutes = $deltaMinutes"""
    }
    //def motionEvents2 = MotionSensor_2?.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
    //def motionEvents3 = MotionSensor_3?.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
    //def Active2 = motionEvents2?.size() != 0
    //def Active3 = motionEvents3?.size() != 0
    //Found ${motionEvents2?.size() ?: 0} events in the last $minutesMotion minutes at $MotionSensor_2
    //Found ${motionEvents3?.size() ?: 0} events in the last $minutesMotion minutes at $MotionSensor_3


    state.Motionhandlerrunning = false
    log.debug "Active = $Active"
    return Active
}

def CheckWindows(){

    //long MessageMinutes = 60*60000 as Long
    //long LastTimeMessageSent = state.LastTimeMessageSent as Long
    //def MessageTimeDelay = now() > LastTimeMessageSent + MessageMinutes 
    //log.debug "MessageTimeDelay = $MessageTimeDelay (checkwindows)"
    // for when it previously failed to turn off thermostats
    def AllContactsClosed = AllContactsAreClosed()
    log.debug "Checking windows"

    def CurrMode = location.currentMode

    def OkToOpen = OkToOpen() // outside and inside temperatures criteria and more... 

    def message = ""

    def allContactsAreOpen = AllContactsAreOpen()

    log.debug "Contacts closed?($AllContactsClosed)"

    def Inside = XtraTempSensor.currentValue("temperature")
    //Inside = Double.parseDouble(Inside)
    //Inside = Inside.toInteger()
    log.debug "Inside = $Inside"
    def Outside = OutsideSensor.currentValue("temperature")
    //Outside = Double.parseDouble(Outside)
    //Outside = Outside.toInteger()
    log.debug "Outside = $Outside"

    log.trace """
OkToOpen?($OkToOpen); 
OffSet?($OffSet) 
state.ClosedByApp($state.ClosedByApp) 
state.OpenByApp($state.OpenByApp) 
state.messageSent($state.messageSent) 

"""
    if(OkToOpen){

        def ClosedByApp = state.ClosedByApp
        def inAway = CurrMode in Away
        def outsideTemp = OutsideSensor?.currentValue("temperature")
        def HSPSet = state.CSPSet.toInteger()
        def WarmEnoughOutside = outsideTemp >= 60

        if(AllContactsClosed){
            log.debug "OpenInfullWhenAway = $OpenInfullWhenAway, inAway = $inAway"
            if( inAway && ClosedByApp != true && OpenInfullWhenAway  && WarmEnoughOutside){
                ClosedByApp = true

            }
            if(ClosedByApp) {
                Actuators?.on()
                if(inAway && OpenInfullWhenAway){
                    ActuatorException?.on()
                }
                state.OpenByApp = true
                state.ClosedByApp = false // so it doesn't open again

                log.debug "opening windows"
                if(OperatingTime){
                    message = "I'm opening windows because $state.causeOpen. Operation time is $OperatingTime seconds"
                    runIn(OperatingTime, StopActuators) 
                }
                else {
                    message = "I'm opening windows because $state.causeOpen"
                }
                log.info message 
                //send(message)
            }
            else { 
                log.debug "Windows have already been opened, doing nothing" 
            }
        }
    }
    // if not ok to open and it is open then close
    else if (state.OpenByApp == true && !AllContactsClosed) {

        Actuators?.off()
        ActuatorException?.off()

        message = "I'm closing windows because $state.causeClose"
        //send(message)
        log.info message 

        state.ClosedByApp = true
        state.OpenByApp = false // so it doesn't close again if user opens it manually
    }

    if(state.EndEval == true){
        Evaluate()
    }
    else {
        log.debug "Evaluate() is busy"
    }
}
def CloseWindows(){
    Actuators?.off()
    ActuatorException?.off()
}
def OkToOpen(){
    def message = ""
    log.debug "Checking if it's O.K. to Open windows"
    def ContactsClosed = AllContactsAreClosed()

    def CSPSet = state.CSPSet
    def HSPSet = state.HSPSet
    CSPSet = CSPSet.toInteger()
    HSPSet = HSPSet.toInteger()

    def CurrMode = location.currentMode
    def Inside = XtraTempSensor.currentValue("temperature")
    //Inside = Double.parseDouble(Inside)
    //Inside = Inside.toInteger()
    def CurrTemp = Inside // as int
    def Outside = OutsideSensor.currentValue("temperature")
    //Outside = Double.parseDouble(Outside)
    //Outside = Outside.toInteger()
    def outsideTemp = Outside as int
        state.outsideTemp = Outside
    def WithinCriticalOffSet = (Inside >= (CriticalTemp - OffSet)) && (Outside >= (CriticalTemp + OffSet))

    def OutsideTempHighThres = ExceptACModes()
    def ExceptHighThreshold1 = ExceptHighThreshold1
    log.debug "test"

    //def humidity = OutsideSensor?.latestValue("humidity")
    def humidity = OutsideSensor.currentValue("temperature")
    //humidity = Double.parseDouble(humidity)
    //humidity = humidity.toInteger()
    //log.debug "Inside = $Inside | Outside = $Outside"
    def WindValue = state.wind
    WindValue = WindValue.toInteger()

    def ItfeelsLike = state.FeelsLike
    ItfeelsLike = ItfeelsLike.toInteger()
    def OutsideFeelsHotter = ItfeelsLike > Outside + 2


    log.debug "Humidity EVAL"
    def TooHumid = false
    if(humidity > HumidityTolerance){
        TooHumid = true
    }
    if((WindValue > 3 || Outside < Inside + 2) && humidity <= HumidityTolerance + 5){
        TooHumid = false
    }



    def OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres && (!OutsideFeelsHotter || OutsideFeelsHotter == null)     
    if(TooHumid){
        OutSideWithinMargin = Outside >= OutsideTempLowThres && Outside <= OutsideTempHighThres - 4 && (!OutsideFeelsHotter || OutsideFeelsHotter == null)
    }

    def ShouldCool = OutSideWithinMargin
    def ShouldHeat = state.ShouldHeat

    if(!ShouldHeat && ShouldCool && CurrTemp >= CSPSet + 3) {
        ShouldCool = false
        log.debug "ShouldCool && ShouldHeat && !OutSideWithinMargin && CurrTemp >= CSPSet ==>> ShouldCool = $ShouldCool"
    }


    def inHomeMode = CurrMode in Home

    def result = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool && !TooHumid && !OutsideFeelsHotter

    /*
if(!WithinCriticalOffSet && OutSideWithinMargin && state.ApptVented != true && inHomeMode)
{
// open windows for just five minutes to vent the place, only when new mode is within Home Modes 
Actuators?.on()
ActuatorException?.on()
state.ApptVented = true
message = "Venting the place for 5 minutes"
log.info message
send(message)
}
else if(!inHomeMode &&  state.ApptVented == true)
{
state.ApptVented = false
runIn(60*5, CloseWindows)
}
*/

    state.OpenInFull = false
    if(OpenWhenEverPermitted && outsideTemp >= HSPSet) { 
        state.OpenInFull = true
    }

    // open all the way when gone?
    if(CurrMode in Away && WithinCriticalOffSet && OpenInfullWhenAway ){
        result = true
        state.OpenInFull = true

    } 
    def CRITICAL = false
    if(state.CRITICAL == true){
        result = false
        CRITICAL = true
        state.OpenByApp = true // so windows will close even if manually opened
    }

    if(!result){
        // preparing a dynamic message which will tell why windows won't open (or fans won't turn on)
        def cause1 = !OutSideWithinMargin
        def cause2 = !WithinCriticalOffSet
        def cause3 = !ShouldCool
        def cause4 = TooHumid
        def cause5 = CurrMode in "$Away"
        def cause6 = CRITICAL

        def causeNotList = [ cause1, cause2, cause3, cause4]

        def causeNotTest = causeNotList.findAll{ val ->
            val == true ? true : false
        }
        def ManyCauses = causeNotTest.size() > 1
        def and2 =""
        def and3 = ""
        def and4 = ""
        def and5 = ""
        def and6 = ""

        if(ManyCauses && cause2){
            and2 = ": " 
        }
        if(ManyCauses && cause3){
            and3 = " and"
        }
        if(ManyCauses && cause4){
            and4 = " and"
        }
        if(ManyCauses && cause5){
            and5 = " and"
        }
        if(ManyCauses && cause6){
            and6 = " and"
        }


        def causeNotMap = [ "outside temperature is not within comfortable margin" : cause1,  
                           "$and2 it is not too hot inside ${XtraTempSensor}'s room" : cause2 , 
                           "$and3 it is too hot $outsideWord" : cause3 ,  
                           "$and4 it is too humid outisde" : cause4, 
                           "$and5 home is in $Away Mode": cause5, 
                           "$and6 it is too cold" : cause6, 
                          ]

        // creates a new map with only the keys that have values = true
        def causeNotOkToOpen = causeNotMap.findAll{it.value == true}
        // now collect the keys from this map 
        causeNotOkToOpen = causeNotOkToOpen.collect{ it.key }
        // build a string without the parentheses 
        def MessageStr = new StringBuilder();
        for (String value : causeNotOkToOpen) {
            MessageStr.append(value);
        }
        causeNotOkToOpen = MessageStr.toString();
        state.causeClose = causeNotOkToOpen

        message = "Windows are closed because $causeNotOkToOpen"
        log.info message
        state.messageclosed = message
        // send a reminder every X minutes 

        long MessageMinutes = 60L*60000L
        long LastTimeMessageSent = state.LastTimeMessageSent
        long SinceLast = LastTimeMessageSent + MessageMinutes

        def MessageTimeDelay = now() > SinceLast
        log.debug "SinceLast = $SinceLast || MessageMinutes = $MessageMinutes || LastTimeMessageSent = $LastTimeMessageSent || MessageTimeDelay = $MessageTimeDelay"

        if(MessageTimeDelay && ContactsClosed) {
            //send(message)
            LastTimeMessageSent = now() as Long
            state.LastTimeMessageSent = LastTimeMessageSent as Long
        }

    }
    // causes for opening windows or turning on fans
    else {
        def cause1 = CurrMode in $Away && WithinCriticalOffSet && OpenInfullWhenAway 
        def cause2 = OutSideWithinMargin && WithinCriticalOffSet && ShouldCool && !TooHumid
        //def cause3 = WithinCriticalOffSet
        //def cause4 = TooHumid

        def causeOktList = [ cause1, cause2 ]
        //log.debug "causeNotList = $causeNotList"
        def causeOkTest = causeOktList.findAll{ val ->
            val == true ? true : false
        }
        def and = ""

        if(cause1 && cause2){
            and = "and"
        }
        def causeOkMap = [ "Home is in $CurrMode and outside and inside temperatures are within safety margins" : cause1,  
                          "$and It is not too humid nor too hot nor cold outside" : cause2 , 
                         ]

        // create a new map with only the keys that have values = true
        def causeOkToOpen = causeOkMap.findAll{it.value == true}
        // now get only the keys from this map 
        causeOkToOpen = causeOkToOpen.collect{ it.key }
        // build a string without the parentheses 
        def MessageStr = new StringBuilder();
        for (String value : causeOkToOpen) {
            MessageStr.append(value);
        }
        causeOkToOpen = MessageStr.toString();
        state.causeOpen = causeOkToOpen

        message = "Windows are open because $causeOkToOpen"

        state.messageopened = message // sent once as push message by checkwindows()

    }

    log.info """
Inside?($Inside), Outside?($Outside), 
Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) 
closed?($ContactsClosed)
OutSideWithinMargin?($OutSideWithinMargin)
Inside is WithinCriticalOffSet?($WithinCriticalOffSet) 
Should Cool (no AC)?($ShouldCool)
Should Heat?($ShouldHeat)
ItfeelsLike ${ItfeelsLike}F
Wind = ${WindValue}mph
HumidityTolerance($HumidityTolerance)
Too Humid?($TooHumid)
Outside Humidity is: $humidity
OutsideFeelsHotter?($OutsideFeelsHotter)
OkToOpen?($result)
"""

    /// FOR TESTS 
    // result = false


    return result
}

def StopActuators(){
    def CurrMode = location.currentMode
    def inAway = CurrMode in Away

    def SlightOpen = state.SlightOpen

    def OpenInFull = state.OpenInFull
    //log.debug "STOP"
    if (Actuators?.hasCommand("stop")/* && !OpenInFull*/){

        if(!inAway && !OpenInfullWhenAway){
            log.debug "SENDING STOP COMMAND"
            Actuators?.stop()
        }
    }
    if (ActuatorException?.hasCommand("stop") /* && !OpenInFull*/){
        if(inAway && OpenInfullWhenAway){
            ActuatorException?.stop()
        }
    }


}
def ActuatorsDelay() {

    def seconds = 5000 as Long
    def secondsLog = seconds / 1000
    def since = now() as Long
    def Delay = since + seconds as Long
    log.trace "seconds($secondsLog), since($since), Delay($Delay)"
    /* while(now() < Delay){
//log.debug "wait $secondsLog seconds"
}

StopActuators()
*/
    runIn(OperatingTime, StopActuators)

}

//miscellaneous 
def ExceptACModes(){
    def OutsideTempHighThres = OutsideTempHighThres
    if(ExceptACMode1 && CurrMode in ExceptACMode1){
        def ToMinus = OutsideTempHighThres
        //log.info "BEFORE CHANGE Inside?($Inside), Outside?($Outside), Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) -----------------------------------"
        def NewOutsideTempHighThres = ToMinus - ExceptHighThreshold1
        //log.debug "Home is in $CurrMode mode, so new high outside's temp threshold is: $NewOutsideTempHighThres = $OutsideTempHighThres - $ExceptHighThreshold1" 
        OutsideTempHighThres = NewOutsideTempHighThres
    }
    else if(ExceptACMode2 && CurrMode in ExceptACMode2){
        def ToMinus = OutsideTempHighThres
        //log.info "BEFORE CHANGE Inside?($Inside), Outside?($Outside), Margin?(LowThres:$OutsideTempLowThres - HighThres:$OutsideTempHighThres) -----------------------------------"
        def NewOutsideTempHighThres = ToMinus - ExceptHighThreshold2
        //log.debug "Home is in $CurrMode mode, so new high outside's temp threshold is: $NewOutsideTempHighThres = $OutsideTempHighThres - $ExceptHighThreshold2" 
        OutsideTempHighThres = NewOutsideTempHighThres
    }
    return OutsideTempHighThres
}
def schedules() { 
    OverrideReset()
    def scheduledTimeA = 1
    def scheduledTimeB = 5

    //schedule("0 0/$scheduledTimeA * * * ?", Evaluate)
    //log.debug "Evaluate scheduled to run every $scheduledTimeA minutes"

    schedule("0 0/$scheduledTimeB * * * ?", polls)
    log.debug "polls scheduled to run every $scheduledTimeB minutes"

    if(Actuators){
        schedule("0 0/$scheduledTimeA * * * ?", CheckWindows)
        log.debug "CheckWindows scheduled to run every $scheduledTimeA minutes"
        CheckWindows()
    }

    Evaluate()

}
def polls(){

    if(OutsideSensor){
        def poll = OutsideSensor.hasCommand("poll")
        def refresh = OutsideSensor.hasCommand("refresh")
        if(poll){
            OutsideSensor.poll()
            log.debug "polling $OutsideSensor"
        }
        else if(refresh){
            OutsideSensor.refresh()
            log.debug "refreshing $OutsideSensor"
        }
        else { 
            log.debug "$OutsideSensor does not support either poll() nor refresh() commands"
        }
    }

}
def send(msg){
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage) {
            // //log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            // //log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

    //log.debug msg
}


