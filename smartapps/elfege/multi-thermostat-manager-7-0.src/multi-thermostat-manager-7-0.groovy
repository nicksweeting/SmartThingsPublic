definition(
    name: " Multi Thermostat Manager 7.0",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description:  "Manage up to 4 thermostats, in parallel with these options: contact sensors, temperature sensor and modes",
    category: "Green Living",
    iconUrl: "http://elfege.com/penrose.jpg",
    iconX2Url: "http://elfege.com/penrose.jpg"
)

preferences {

    page name: "pageSetup"
    page name: "settings"
    page name: "Modes"

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
        section("About this App:"){
            paragraph "Manage one or more thermostats under different modes" 
            //paragraph image: "http://elfege.com/penrose.jpg",
        }
        section("Setup Menu") {
            href "settings", title: "Choose your thermostats", description: ""
            href "Modes", title: "Set different temperatures depending on location mode", description: ""
        }
    }
}
def settings() {

    def pageName = "settings"

    def pageProperties = [
        name:       "settings",
        title:      "Select your thermostats",
        nextPage:   "pageSetup"
    ]

    dynamicPage(pageProperties) {

        section("how many thermostats do you want to control?") { 

            input(name: "HowMany", type: "number", range: "1..4", title: "set a value between 1 and 4", description: null, submitOnChange: true)

            if(HowMany >= 1) {
                input(name: "Thermostat_1", type: "capability.thermostat", title: "Thermostat 1 is $Thermostat_1", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_1", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_1){
                    input(name: "Sensor_1", type: "capability.temperatureMeasurement", title: "Pick a sensor", required: true, multiple: false, description: null, uninstall: true)
                }
            }
            if(HowMany >= 2) {
                input(name: "Thermostat_2", type: "capability.thermostat", title: "Thermostat 2 is $Thermostat_2", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_2", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_2){
                    input(name: "Sensor_2", type: "capability.temperatureMeasurement", title: "Pick a sensor", required: true, multiple: false, description: null, uninstall: true)
                }
            }
            if(HowMany >= 3) {
                input(name: "Thermostat_3", type: "capability.thermostat", title: "Thermostat 3 is $Thermostat_3", required: false, multiple: false, description: null, submitOnChange: true)
                input(name: "AltSensor_3", type: "bool", title: "Control this thermostat's states using a third party sensor", required: false, default: false, submitOnChange: true)
                if(AltSensor_3){
                    input(name: "Sensor_3", type: "capability.temperatureMeasurement", title: "Pick a sensor", required: true, multiple: false, description: null, uninstall: true)
                }
            }
            if(HowMany == 4) {
                input(name: "Thermostat_4", type: "capability.thermostat", title: "Thermostat 4 is $Thermostat_4", required: false, multiple: false, description: null, submitOnChange: true)
            }

            if(AltSensor1 || AltSensor_2 || AltSensor_3){
                input(name: "OutsideSensor", type: "capability.temperatureMeasurement", title: "Pick a sensor for Outside's temperature", required: true, multiple: false, description: null)
                paragraph "This sensor is rendered mandatory by selecting alternate sensor management (above). If you do not have an outside temp measurment device, you can allways create a SmartWeater virtual device"
            }

            input(name: "contact", type:"capability.contactSensor", title: "select windows / contacts", multiple: true, required: true)

            input(name: "TimeBeforeClosing", type: "number", title: "after this amount of time in seconds", required: false, description: "default is 60 seconds", default: 60, uninstall: true, install: true)

            input(name: "CriticalTemp", type:"number", title: "but do not allow the temperature to fall bellow this value", required: true, decription: "set a safety value, default is 65", defaut: 65)
            input(name: "XtraTempSensor", type:"capability.temperatureMeasurement", title: "select a temperature sensor that will serve as reference", required: true)
        }    
    }
}
def Modes(){

    def pageName = "Modes"

    def pageProperties = [
        name:       "Modes",
        title:      "Select Modes and Temperatures",
        nextPage:   "pageSetup"
    ]

    dynamicPage(pageProperties) {

        section("Main Mode") {
            input(name: "Home", type : "mode", title: "Select modes for when you're at home", multiple: true, required: false)

        }
        section("Other Modes"){
            input(name: "Night", type : "mode", title: "Select Night mode(s)", multiple: true, required: true)
            input(name: "Away", type : "mode", title: "Select away mode(s)", multiple: true, required: true)
        }
        section("MoreModes"){ 
            input(name: "Moremodes", type: "bool", title: "add more modes", required: false, defaut: false, submitOnChange: true)
            if(Moremodes){
                input(name: "CustomMode1", type : "mode", title: "Select modes", multiple: true, required: true)
                input(name: "CustomMode2", type : "mode", title: "Select modes", multiple: true, required: false, submitOnChange: true)
            }
        }
        section("Thermostats temperatures for $Home Mode"){
            if(HowMany >= 1) {
                input(name: "HSPH1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $Home mode", required: true)
                input(name: "CSPH1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $Home mode", required: true)
            }
            if(HowMany >= 2) {
                input(name: "HSPH2", type: "decimal", title: "Set Heating temperature for $Thermostat_2 in $Home mode", required: true)
                input(name: "CSPH2", type: "decimal", title: "Set Cooling temperature for $Thermostat_2 in $Home mode", required: true)
            }
            if(HowMany >= 3) {     
                input(name: "HSPH3", type: "decimal", title: "Set Heating temperature for $Thermostat_3 in $Home mode", required: true)
                input(name: "CSPH3", type: "decimal", title: "Set Cooling temperature for $Thermostat_3 in $Home mode", required: true)
            }
            if(HowMany == 4) {   
                input(name: "HSPH4", type: "decimal", title: "Set Heating temperature for $Thermostat_4 in $Home mode", required: true)
                input(name: "CSPH4", type: "decimal", title: "Set Cooling temperature for $Thermostat_4 in $Home mode", required: true)
            }
        }
        section("Thermostats temperatures for $Night Mode"){
            if(HowMany >= 1) {
                input(name: "HSPN1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $Night mode", required: true)
                input(name: "CSPN1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $Night mode", required: true)
            }
            if(HowMany >= 2) {
                input(name: "HSPN2", type: "decimal", title: "Set Heating temperature for $Thermostat_2 in $Night mode", required: true)
                input(name: "CSPN2", type: "decimal", title: "Set Cooling temperature for $Thermostat_2 in $Night mode", required: true)
            }
            if(HowMany >= 3) {     
                input(name: "HSPN3", type: "decimal", title: "Set Heating temperature for $Thermostat_3 in $Night mode", required: true)
                input(name: "CSPN3", type: "decimal", title: "Set Cooling temperature for $Thermostat_3 in $Night mode", required: true)
            }
            if(HowMany == 4) {   
                input(name: "HSPN4", type: "decimal", title: "Set Heating temperature for $Thermostat_4 in $Night mode", required: true)
                input(name: "CSPN4", type: "decimal", title: "Set Cooling temperature for $Thermostat_4 in $Night mode", required: true)
            }
        }
        section("Thermostats temperatures for $Away Mode"){   
            paragraph "these values apply to all thermostats evenly"
            input(name: "HSPA", type: "decimal", title: "Set Heating temperature for $Away mode", required: true)
            input(name: "CSPA", type: "decimal", title: "Set Cooling temperature for $Away mode", required: true)
        }

        if(Moremodes){
            section("$CustomMode1 Mode"){
                if(HowMany >= 1) {
                    input(name: "HSPCust1_T1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $CustomMode1 mode", required: true)
                    input(name: "CSPCust1_T1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $CustomMode1 mode", required: true)
                }
                if(HowMany >= 2) {
                    input(name: "HSPCust1_T2", type: "decimal", title: "Set Heating temperature for $Thermostat_2 in $CustomMode1 mode", required: true)
                    input(name: "CSPCust1_T2", type: "decimal", title: "Set Cooling temperature for $Thermostat_2 in $CustomMode1 mode", required: true)
                }
                if(HowMany >= 3) {     
                    input(name: "HSPCust1_T3", type: "decimal", title: "Set Heating temperature for $Thermostat_3 in $CustomMode1 mode", required: true)
                    input(name: "CSPCust1_T3", type: "decimal", title: "Set Cooling temperature for $Thermostat_3 in $CustomMode1 mode", required: true)
                }
                if(HowMany == 4) {   
                    input(name: "HSPCust1_T4", type: "decimal", title: "Set Heating temperature for $Thermostat_4 in $CustomMode1 mode", required: true)
                    input(name: "CSPCust1_T4", type: "decimal", title: "Set Cooling temperature for $Thermostat_4 in $CustomMode1 mode", required: true)
                }
            }
            if(CustomMode2){
                section("$CustomMode2 Mode"){
                    if(HowMany >= 1) {
                        input(name: "HSPCust2_T1", type: "decimal", title: "Set Heating temperature for $Thermostat_1 in $CustomMode2 mode", required: true)
                        input(name: "CSPCust2_T1", type: "decimal", title: "Set Cooling temperature for $Thermostat_1 in $CustomMode2 mode", required: true)
                    }
                    if(HowMany >= 2) {
                        input(name: "HSPCust2_T2", type: "decimal", title: "Set Heating temperature for $Thermostat_2 in $CustomMode2 mode", required: true)
                        input(name: "CSPCust2_T2", type: "decimal", title: "Set Cooling temperature for $Thermostat_2 in $CustomMode2 mode", required: true)
                    }
                    if(HowMany >= 3) {     
                        input(name: "HSPCust3_T3", type: "decimal", title: "Set Heating temperature for $Thermostat_3 in $CustomMode2 mode", required: true)
                        input(name: "CSPCust3_T3", type: "decimal", title: "Set Cooling temperature for $Thermostat_3 in $CustomMode2 mode", required: true)
                    }
                    if(HowMany == 4) {   
                        input(name: "HSPCust4_T4", type: "decimal", title: "Set Heating temperature for $Thermostat_4 in $CustomMode2 mode", required: true)
                        input(name: "CSPCust4_T4", type: "decimal", title: "Set Cooling temperature for $Thermostat_4 in $CustomMode2 mode", required: true)
                    }
                }
            }
        }
    }
}

def installed() {	 
    log.debug "enter installed, state: $state"	

    init()
}
def updated() {
    unsubscribe()
    unschedule()
    log.debug "Current mode = ${location.mode}"    
    log.debug "updated with settings = $settings $Modes"

    init()
}
def init() {

    log.debug "enter updated, state: $state"  
    log.debug "updated with settings = $settings $Modes"


    pollThermostats()
    log.debug "polling thermostats"
    recordCurrentStates()
    log.debug "recording states"

    subscribe(contact, "contact.open", contactHandlerOpen)
    subscribe(contact, "contact.closed", contactHandlerClosed)
    subscribe(XtraTempSensor, "temperature", temperatureHandler)
    subscribe(location, "mode", ChangedModeHandler)	

    subscribe(Thermostat_1, "temperature", temperatureHandler)
    if(Thermostat_2){
        subscribe(Thermostat_2, "temperature", temperatureHandler)
    }
    if(Thermostat_3){
        subscribe(Thermostat_3, "temperature", temperatureHandler)
    }
    if(Thermostat_4){
        subscribe(Thermostat_4, "temperature", temperatureHandler)
    }

    if(AltSensor_1 || AltSensor_2 || AltSensor_3){
        subscribe(OutsideSensor, "temperature", temperatureHandler)
    }

    if(AltSensor_1){
        subscribe(Sensor_1, "temperature", temperatureHandler)
        log.debug "Subscription for alternative Sensor for $Sensor_1"
        AlternativeSensor1()
    }
    if(AltSensor_2){
        subscribe(Sensor_2, "temperature", temperatureHandler)
        log.debug "Subscription for alternative Sensor for $Sensor_2"
        AlternativeSensor2()
    }
    if(AltSensor_3){
        subscribe(Sensor_3, "temperature", temperatureHandler)
        log.debug "Subscription for alternative Sensor for $Sensor_3"
        AlternativeSensor3()
    }

    def scheduledTime = 1
    if(AltSensor_1){
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor1)
        log.debug "AlternativeSensor1 scheduled to run every $scheduledTime minutes"
        AlternativeSensor1()
    }
    if(AltSensor_2){
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor2)
        log.debug "AlternativeSensor2 scheduled to run every $scheduledTime minutes"
        AlternativeSensor2()
    }
    if(AltSensor_3){
        schedule("0 0/$scheduledTime * * * ?", AlternativeSensor3)
        log.debug "AlternativeSensor3 scheduled to run every $scheduledTime minutes"
        AlternativeSensor3()
    }

    schedule("0 0/$scheduledTime * * * ?", TemperaturesModes)
    log.debug "TemperaturesModes scheduled to run every $scheduledTime minutes"
    TemperaturesModes()
}
def temperatureHandler(evt) { 

    log.debug "current temperature value for $evt.device is $evt.value" 
    def currentTemp = XtraTempSensor.currentValue("temperature")

    if(AltSensor_1){
        AlternativeSensor1()
    }
    if(AltSensor_2){
        AlternativeSensor2()
    }
    if(AltSensor_3){
        AlternativeSensor3()
    }

    if(currentTemp < CriticalTemp) {
        log.debug "EMERGENCY HEATING - TEMPERATURE IS TOO LOW!" 
        TurnOnThermostats()
    } 
    else 
    { 
        log.debug "CriticalTemp OK"
    } 
    TemperaturesModes()
}
def AlternativeSensor1(){
    log.debug "Running Alternative Sensor Loop for $Thermostat_1"
    def SenTemp = Sensor_1.currentTemperature
    log.debug "Current Temperature at $Sensor_1 is ${SenTemp}F"
    def OutsideTemp = OutsideSensor.currentTemperature
    def NewHeatSet = 0 
    def NewCoolSet = 0

    def CurrMode = location.currentMode

    def DefaultSetHeat = null
    def DefaultSetCool = null

    if(CurrMode in Home){
        DefaultSetHeat = HSPH1
        DefaultSetCool = CSPH1
    }
    else if(CurrMode in Night){
        DefaultSetHeat = HSPN1
        DefaultSetCool = CSPN1
    }
    else if(CurrMode in Away){
        DefaultSetHeat = HSPA
        DefaultSetCool = CSPA
    }
    else if(CurrMode in CustumMode1){
        DefaultSetHeat = HSPCust1_T1
        DefaultSetCool = CSPCust1_T1
    }
    else if(CurrMode in CustumMode2){
        DefaultSetHeat = HSPCust2_T1
        DefaultSetCool = CSPCust2_T1
    }

    if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
        // incresease current thermostat heat setting to force run 
        NewHeatSet = DefaultSetHeat + 5
        NewCoolSet = DefaultSetCool - 5

        log.trace "$Thermostat_1: DefaultSetHeat = $DefaultSetHeat, DefaultSetCool = $DefaultSetCool, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
        Thermostat_1.setHeatingSetpoint(NewHeatSet)
        log.debug "$Thermostat_1 heating now set to $NewHeatSet"
        Thermostat_1.setCoolingSetpoint(NewCoolSet)
        log.debug "$Thermostat_1 cooling now set to $NewCoolSet"

        // set proper mode
        if(SenTemp < DefaultSetHeat){
            Thermostat_1.setThermostatMode("heat")
            log.debug "$Thermostat_1 set to Heat"
        }
        if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){
            Thermostat_1.setThermostatMode("cool")
            log.debug "$Thermostat_1 set to Cool"
        }
    } 
    else {
        //turning off this unit
        state.ThermState = Thermostat_1.currentValue("thermostatMode") as String
        if(state.ThermState == "off"){
            log.debug "$Thermostat_1 stays off"
        }
        else {
            Thermostat_1.setThermostatMode("off")
            log.debug "turning off $Thermostat_1"      
        }
    }
}
def AlternativeSensor2(){
    log.debug "Running Alternative Sensor Loop for $Thermostat_2"
    def SenTemp = Sensor_2.currentTemperature
    log.debug "Current Temperature at $Sensor_2 is ${SenTemp}F"
    def OutsideTemp = OutsideSensor.currentTemperature
    def NewHeatSet = 0 
    def NewCoolSet = 0
    def CurrMode = location.currentMode

    def DefaultSetHeat = null
    def DefaultSetCool = null

    if(CurrMode in Home){
        DefaultSetHeat = HSPH2
        DefaultSetCool = CSPH2
    }
    else if(CurrMode in Night){
        DefaultSetHeat = HSPN2
        DefaultSetCool = CSPN2
    }
    else if(CurrMode in Away){
        DefaultSetHeat = HSPA
        DefaultSetCool = CSPA
    }
    else if(CurrMode in CustumMode1){
        DefaultSetHeat = HSPCust1_T2
        DefaultSetCool = CSPCust1_T2
    }
    else if(CurrMode in CustumMode2){
        DefaultSetHeat = HSPCust2_T2
        DefaultSetCool = CSPCust2_T2
    }

    if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
        // incresease current thermostat heat setting to force run 
        NewHeatSet = DefaultSetHeat + 5
        NewCoolSet = DefaultSetCool - 5

        log.trace "$Thermostat_2: DefaultSetHeat = $DefaultSetHeat, DefaultSetCool = $DefaultSetCool, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
        Thermostat_2.setHeatingSetpoint(NewHeatSet)
        log.debug "$Thermostat_2 heating now set to $NewHeatSet"
        Thermostat_2.setCoolingSetpoint(NewCoolSet)
        log.debug "$Thermostat_2 cooling now set to $NewCoolSet"

        // set proper mode
        if(SenTemp < DefaultSetHeat){
            Thermostat_2.setThermostatMode("heat")
            log.debug "$Thermostat_2 set to Heat"
        }
        if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){
            Thermostat_2.setThermostatMode("cool")
            log.debug "$Thermostat_2 set to Cool"
        }
    } 
    else {
        //turning off this unit
        state.ThermState = Thermostat_2.currentValue("thermostatMode") as String
        if(state.ThermState == "off"){
            log.debug "$Thermostat_2 stays off"
        }
        else {
            Thermostat_2.setThermostatMode("off")
            log.debug "turning off $Thermostat_2"      
        }
    }
}
def AlternativeSensor3(){
    log.debug "Running Alternative Sensor Loop for $Thermostat_3"
    def SenTemp = Sensor_3.currentTemperature
    log.debug "Current Temperature at $Sensor_3 is ${SenTemp}F"
    def OutsideTemp = OutsideSensor.currentTemperature
    def NewHeatSet = 0 
    def NewCoolSet = 0
    def CurrMode = location.currentMode

    def DefaultSetHeat = null
    def DefaultSetCool = null

    if(CurrMode in Home){
        DefaultSetHeat = HSPH3
        DefaultSetCool = CSPH3
    }
    else if(CurrMode in Night){
        DefaultSetHeat = HSPN3
        DefaultSetCool = CSPN3
    }
    else if(CurrMode in Away){
        DefaultSetHeat = HSPA
        DefaultSetCool = CSPA
    }
    else if(CurrMode in CustumMode1){
        DefaultSetHeat = HSPCust1_T3
        DefaultSetCool = CSPCust1_T3
    }
    else if(CurrMode in CustumMode2){
        DefaultSetHeat = HSPCust2_T3
        DefaultSetCool = CSPCust2_T3
    }

    if(SenTemp < DefaultSetHeat || OutsideTemp > SenTemp){
        // incresease current thermostat heat setting to force run 
        NewHeatSet = DefaultSetHeat + 5
        NewCoolSet = DefaultSetCool - 5

        log.trace "$Thermostat_3: DefaultSetHeat = $DefaultSetHeat, DefaultSetCool = $DefaultSetCool, NewHeatSet = $NewHeatSet, NewCoolSet = $NewCoolSet"
        Thermostat_3.setHeatingSetpoint(NewHeatSet)
        log.debug "$Thermostat_3 heating now set to $NewHeatSet"
        Thermostat_3.setCoolingSetpoint(NewCoolSet)
        log.debug "$Thermostat_3 cooling now set to $NewCoolSet"

        // set proper mode
        if(SenTemp < DefaultSetHeat){
            Thermostat_3.setThermostatMode("heat")
            log.debug "$Thermostat_3 set to Heat"
        }
        if(SenTemp > DefaultSetCool /* making sure it doesn't cool after heating --> */ && OutsideTemp > DefaultSetCool){
            Thermostat_3.setThermostatMode("cool")
            log.debug "$Thermostat_3 set to Cool"
        }
    } 
    else {
        //turning off this unit
        state.ThermState = Thermostat_3.currentValue("thermostatMode") as String
        if(state.ThermState == "off"){
            log.debug "$Thermostat_3 stays off"
        }
        else {
            Thermostat_3.setThermostatMode("off")
            log.debug "turning off $Thermostat_3"      
        }
    }
}
def contactHandlerClosed(evt) {

    log.debug "$evt.device is $evt.value"

    if(contact.latestValue("contact").contains("open")){
        log.debug "Not all contacts are closed, doing nothing"
    }
    else {
        unschedule(TurnOffThermostats) 
        log.debug "unscheduling previous TurnOffThermostats command"
        state.doorsAreOpen = 0
        TurnOnThermostats()
    } 
}
def contactHandlerOpen(evt) {

    log.debug "$evt.device is now $evt.value"
    state.doorsAreOpen = 1
    log.debug "recording latest states"
    recordCurrentStates()

    log.debug "Turning off all thermostats in $TimeBeforeClosing seconds"
    runIn(TimeBeforeClosing, TurnOffThermostats)      

}
def ChangedModeHandler(evt) {

    state.modeStartTime = now() 

    log.debug "mode changed to ${evt.value}"



    TemperaturesModes()
}
def TemperaturesModes(){
    def CurrMode = location.currentMode
    def outsideTemp = OutsideSensor.currentTemperature

    if(Thermostat_1){
        state.CurrTemp1 = Thermostat_1.currentTemperature
        state.ThermState1 = Thermostat_1.currentValue("thermostatMode") 
    }
    if(Thermostat_2){
        state.CurrTemp2 = Thermostat_2.currentTemperature
        state.therm_2_CurrMode = Thermostat_2.currentValue("thermostatMode") as String
        log.debug "$Thermostat_2 mode is recorded as: $state.therm_2_CurrMode"
        state.ThermState2 = state.therm_2_CurrMode
    }
    if(Thermostat_3){
        state.CurrTemp3 = Thermostat_3.currentTemperature
        state.ThermState3 = Thermostat_3.currentValue("thermostatMode")
    }
    if(Thermostat_4){
        state.CurrTemp4 = Thermostat_4.currentTemperature
        state.ThermState4 = Thermostat_4.currentValue("thermostatMode")
    }

    log.trace "$Thermostat_1 : $state.ThermState1, $Thermostat_2 : $state.ThermState2, $Thermostat_3 : $state.ThermState3, $Thermostat_4 : $state.ThermState4"

    log.trace "CURRENT TEMPS : state.CurrTemp1 : $state.CurrTemp1, state.CurrTemp2 : $state.CurrTemp2, state.CurrTemp3 : $state.CurrTemp3, state.CurrTemp4 : $state.CurrTemp4, OUTSIDE: $outsideTemp"

    if(CurrMode in Home){

        log.debug "location is in $CurrMode mode, applying settings accordingly" 
        if(Thermostat_1){
            if(!AltSensor_1){
                log.debug "loading $Home settings for $Thermostat_1"        
                Thermostat_1.setHeatingSetpoint(HSPH1)
                Thermostat_1.setCoolingSetpoint(CSPH1)

                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp1 > HSPH1 && state.ThermState1 != "off"){
                    Thermostat_1.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPH1 && state.ThermState1 != "cool"){
                    Thermostat_1.setThermostatMode("cool")
                }
                else if(state.CurrTemp1 < HSPH1 && state.ThermState1 != "heat"){
                    Thermostat_1.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_2){
            if(!AltSensor_2){
                log.debug "loading $Home settings for $Thermostat_2"          
                Thermostat_2.setHeatingSetpoint(HSPH2)
                Thermostat_2.setCoolingSetpoint(CSPH2)  


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp2 > HSPH2 && state.ThermState2 != "off"){
                    Thermostat_2.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPH2 && state.ThermState2 != "cool"){
                    Thermostat_2.setThermostatMode("cool")
                }
                else if(state.CurrTemp2 < HSPH2 && state.ThermState2 != "heat"){
                    Thermostat_2.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_3){
            if(!AltSensor_3){
                log.debug "loading $Home settings for $Thermostat_3"
                Thermostat_3.setHeatingSetpoint(HSPH3)
                Thermostat_3.setCoolingSetpoint(CSPH3)

                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp3 > HSPH3 && state.ThermState3 != "off"){
                    Thermostat_3.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPH3 && state.ThermState3 != "cool"){
                    Thermostat_3.setThermostatMode("cool")
                }
                else if(state.CurrTemp3 < HSPH3 && state.ThermState3 != "heat"){
                    Thermostat_3.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_4){
            log.debug "loading $Home settings for $Thermostat_4"
            Thermostat_4.setHeatingSetpoint(HSPH4)
            Thermostat_4.setCoolingSetpoint(CSPH4)   

            // no AltSensor 4 
            if(state.CurrTemp4 > HSPH4 && state.ThermState4 != "off"){
                Thermostat_4.setThermostatMode("off")
            } 
            else if(outsideTemp >= CSPH4 && state.ThermState4 != "cool"){
                Thermostat_4.setThermostatMode("cool")
            }
            else if(state.CurrTemp4 < HSPH4 && state.ThermState4 != "heat"){
                Thermostat_4.setThermostatMode("heat")

            }
        }
    }
    else if(CurrMode in Night){
        log.debug "location is in $CurrMode mode, applying settings accordingly" 
        if(Thermostat_1){
            if(!AltSensor_1){
                log.debug "loading $Night settings for $Thermostat_1"
                Thermostat_1.setHeatingSetpoint(HSPN1)
                Thermostat_1.setCoolingSetpoint(CSPN1)  


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp1 > HSPN1 && state.ThermState1 != "off"){
                    Thermostat_1.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPN1 && state.ThermState1 != "cool"){
                    Thermostat_1.setThermostatMode("cool")
                }
                else if(state.CurrTemp1 < HSPN1 && state.ThermState1 != "heat"){
                    Thermostat_1.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_2){
            if(!AltSensor_2){
                log.debug "loading $Night settings for $Thermostat_2"
                Thermostat_2.setHeatingSetpoint(HSPN2)
                Thermostat_2.setCoolingSetpoint(CSPN2) 


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp2 > HSPN2 && state.ThermState2 != "off"){
                    Thermostat_2.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPN2 && state.ThermState2 != "cool"){
                    Thermostat_2.setThermostatMode("cool")
                }
                else if(state.CurrTemp2 < HSPN2 && state.ThermState2 != "heat"){
                    Thermostat_2.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_3){
            if(!AltSensor_3){
                log.debug "loading $Night settings for $Thermostat_3"
                Thermostat_3.setHeatingSetpoint(HSPN3)
                Thermostat_3.setCoolingSetpoint(CSPN3)  


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp3 > HSPN3 && state.ThermState3 != "off"){
                    Thermostat_3.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPN3 && state.ThermState3 != "cool"){
                    Thermostat_3.setThermostatMode("cool")
                }
                else if(state.CurrTemp3 < HSPN3 && state.ThermState3 != "heat"){
                    Thermostat_3.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_4){
            log.debug "loading $Night0 settings for $Thermostat_4"
            Thermostat_4.setHeatingSetpoint(HSPN4)
            Thermostat_4.setCoolingSetpoint(CSPN4)    

            // no AltSensor 4 
            if(state.CurrTemp4 > HSPN4 && state.ThermState4 != "off"){
                Thermostat_4.setThermostatMode("off")
            } 
            else if(outsideTemp >= CSPN4 && state.ThermState4 != "cool"){
                Thermostat_4.setThermostatMode("cool")
            }
            else if(state.CurrTemp4 < HSPN4 && state.ThermState4 != "heat"){
                Thermostat_4.setThermostatMode("heat")
            }           
        }
    }
    else if(CurrMode in Away){
        log.debug "location is in $CurrMode mode, applying settings accordingly" 
        if(Thermostat_1){
            if(!AltSensor_1){
                log.debug "loading $Away settings for $Thermostat_1"
                Thermostat_1.setHeatingSetpoint(HSPA1)
                Thermostat_1.setCoolingSetpoint(CSPA1)


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp1 > HSPA1 && state.ThermState1 != "off"){
                    Thermostat_1.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPA1 && state.ThermState1 != "cool"){
                    Thermostat_1.setThermostatMode("cool")
                }
                else if(state.CurrTemp1 < HSPA1 && state.ThermState1 != "heat"){
                    Thermostat_1.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_2){
            if(!AltSensor_2){
                log.debug "loading $Away settings for $Thermostat_2"
                Thermostat_2.setHeatingSetpoint(HSPA2)
                Thermostat_2.setCoolingSetpoint(CSPA2)  

                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp2 > HSPA2 && state.ThermState2 != "off"){
                    Thermostat_2.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPA2 && state.ThermState2 != "cool"){
                    Thermostat_2.setThermostatMode("cool")
                }
                else if(state.CurrTemp2 < HSPA2 && state.ThermState2 != "heat"){
                    Thermostat_2.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_3){
            if(!AltSensor_3){
                log.debug "loading $Away settings for $Thermostat_3"
                Thermostat_3.setHeatingSetpoint(HSPA3)
                Thermostat_3.setCoolingSetpoint(CSPA3)   


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp3 > HSPA3 && state.ThermState3 != "off"){
                    Thermostat_3.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPA3 && state.ThermState3 != "cool"){
                    Thermostat_3.setThermostatMode("cool")
                }
                else if(state.CurrTemp3 < HSPA3 && state.ThermState3 != "heat"){
                    Thermostat_3.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_4){
            log.debug "loading $Away settings for $Thermostat_4"
            Thermostat_4.setHeatingSetpoint(HSPA4)
            Thermostat_4.setCoolingSetpoint(CSPA4)   

            // no AltSensor 4 
            if(state.CurrTemp4 > HSPA4 && state.ThermState4 != "off"){
                Thermostat_4.setThermostatMode("off")
            } 
            else if(outsideTemp >= CSPA4 && state.ThermState4 != "cool"){
                Thermostat_4.setThermostatMode("cool")
            }
            else if(state.CurrTemp4 < HSPA4 && state.ThermState4 != "heat"){
                Thermostat_4.setThermostatMode("heat")
            }           
        }
    }
    else if(CurrMode in CustomMode1){
        log.debug "location is in $CurrMode mode, applying settings accordingly" 
        if(Thermostat_1){
            if(!AltSensor_1){
                log.debug "loading $CustomMode1 settings for $Thermostat_1"
                Thermostat_1.setHeatingSetpoint(HSPCust1_T1)
                Thermostat_1.setCoolingSetpoint(CSPCust1_T1) 


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp1 > HSPCust1_T1 && state.ThermState1 != "off"){
                    Thermostat_1.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPCust1_T1 && state.ThermState1 != "cool"){
                    Thermostat_1.setThermostatMode("cool")
                }
                else if(state.CurrTemp1 < HSPCust1_T1 && state.ThermState1 != "heat"){
                    Thermostat_1.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_2){
            if(!AltSensor_2){
                log.debug "loading $CustomMode1 settings for $Thermostat_2"
                Thermostat_2.setHeatingSetpoint(HSPCust1_T2)
                Thermostat_2.setCoolingSetpoint(CSPCust1_T2)  


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp2 > HSPCust1_T2 && state.ThermState2 != "off"){
                    Thermostat_2.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPCust1_T2 && state.ThermState2 != "cool"){
                    Thermostat_2.setThermostatMode("cool")
                }
                else if(state.CurrTemp2 < HSPCust1_T2 && state.ThermState2 != "heat"){
                    Thermostat_2.setThermostatMode("heat")
                }
            }      
        }
        if(Thermostat_3){
            if(!AltSensor_3){
                log.debug "loading $CustomMode1 settings for $Thermostat_3"
                Thermostat_3.setHeatingSetpoint(HSPCust1_T3)
                Thermostat_3.setCoolingSetpoint(CSPCust1_T3)   


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp3 > HSPCust1_T3 && state.ThermState3 != "off"){
                    Thermostat_3.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPCust1_T3 && state.ThermState3 != "cool"){
                    Thermostat_3.setThermostatMode("cool")
                }
                else if(state.CurrTemp3 < HSPCust1_T3 && state.ThermState3 != "heat"){
                    Thermostat_3.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_4){
            log.debug "loading $CustomMode1 settings for $Thermostat_4"
            Thermostat_4.setHeatingSetpoint(HSPCust1_T4)
            Thermostat_4.setCoolingSetpoint(CSPCust1_T4)     

            // no AltSensor 4 
            if(state.CurrTemp4 > HSPCust1_T4 && state.ThermState4 != "off"){
                Thermostat_4.setThermostatMode("off")
            } 
            else if(outsideTemp >= CSPCust1_T4 && state.ThermState4 != "cool"){
                Thermostat_4.setThermostatMode("cool")
            }
            else if(state.CurrTemp4 < HSPCust1_T4 && state.ThermState4 != "heat"){
                Thermostat_4.setThermostatMode("heat")
            }   
        }
    }
    else if(CustomMode2 && CurrMode in CustomMode2){
        if(Thermostat_1){
            if(!AltSensor_1){
                log.debug "loading $CustomMode2 settings for $Thermostat_1"
                Thermostat_1.setHeatingSetpoint(HSPCust2_T1)
                Thermostat_1.setCoolingSetpoint(CSPCust2_T1)    


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp1 > HSPCust2_T1 && state.ThermState1 != "off"){
                    Thermostat_1.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPCust2_T1 && state.ThermState1 != "cool"){
                    Thermostat_1.setThermostatMode("cool")
                }
                else if(state.CurrTemp1 < HSPCust2_T1 && state.ThermState1 != "heat"){
                    Thermostat_1.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_2){
            if(!AltSensor_2){
                log.debug "loading $CustomMode2 settings for $Thermostat_2"
                Thermostat_2.setHeatingSetpoint(HSPCust2_T1)
                Thermostat_2.setCoolingSetpoint(CSPCust2_T2)


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp2 > HSPCust2_T1 && state.ThermState2 != "off"){
                    Thermostat_2.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPCust2_T2 && state.ThermState2 != "cool"){
                    Thermostat_2.setThermostatMode("cool")
                }
                else if(state.CurrTemp2 < HSPCust2_T1 && state.ThermState2 != "heat"){
                    Thermostat_2.setThermostatMode("heat")
                }
            }      
        }
        if(Thermostat_3){
            if(!AltSensor_3){
                log.debug "loading $CustomMode2 settings for $Thermostat_3"
                Thermostat_3.setHeatingSetpoint(HSPCust2_T3)
                Thermostat_3.setCoolingSetpoint(CSPCust2_T3) 


                // if AltSensor then these controls are set by AltSensor loop so we avoid a conflict
                if(state.CurrTemp3 > HSPCust2_T3 && state.ThermState3 != "off"){
                    Thermostat_3.setThermostatMode("off")
                } 
                else if(outsideTemp >= CSPCust2_T3 && state.ThermState3 != "cool"){
                    Thermostat_3.setThermostatMode("cool")
                }
                else if(state.CurrTemp3 < HSPCust2_T3 && state.ThermState3 != "heat"){
                    Thermostat_3.setThermostatMode("heat")
                }
            }
        }
        if(Thermostat_4){
            log.debug "loading $CustomMode2 settings for $Thermostat_4"
            Thermostat_4.setHeatingSetpoint(HSPCust2_T4)
            Thermostat_4.setCoolingSetpoint(CSPCust2_T4)  

            // no AltSensor 4 
            if(state.CurrTemp4 > HSPCust2_T4 && state.ThermState4 != "off"){
                Thermostat_4.setThermostatMode("off")
            } 
            else if(outsideTemp >= CSPCust2_T4 && state.ThermState4 != "cool"){
                Thermostat_4.setThermostatMode("cool")
            }
            else if(state.CurrTemp4 < HSPCust2_T4 && state.ThermState4 != "heat"){
                Thermostat_4.setThermostatMode("heat")
            }   
        }
    }
   
}
def recordCurrentStates(){

    state.therm_1_CurrMode = Thermostat_1.currentValue("thermostatMode") as String
    log.debug "$Thermostat_1 mode is recoreded as: $state.therm_1_CurrMode"
    if(Thermostat_2){
        state.therm_2_CurrMode = Thermostat_2.currentValue("thermostatMode") as String
        log.debug "$Thermostat_2 mode is recoreded as: $state.therm_2_CurrMode"
        if(Thermostat_3){
            state.therm_3_CurrMode = Thermostat_3.currentValue("thermostatMode") as String
            log.debug "$Thermostat_3 mode is recoreded as: $state.therm_3_CurrMode"
            if(Thermostat_4){
                state.therm_4_CurrMode = Thermostat_4.currentValue("thermostatMode") as String
                log.debug "$Thermostat_4 mode is recoreded as: $state.therm_4_CurrMode"
            }
        }
    }
}
def TurnOffThermostats() {
    log.debug "Turning off thermostats" 
    Thermostat_1.setThermostatMode("off")
    log.debug "$Thermostat_1  turned off"
    if(Thermostat_2){      
        Thermostat_2.setThermostatMode("off")
        log.debug "$Thermostat_2 turned off"
    }
    if(Thermostat_3){
        Thermostat_3.setThermostatMode("off")
        log.debug "$Thermostat_3 turned off"
    }
    if(Thermostat_4){
        if(Thermostat_4.currentValue != state.therm_4_CurrMode){
            Thermostat_4.setThermostatMode("off")
            log.debug "$Thermostat_4 turned off"
        }
    }
}
def TurnOnThermostats()   {
    log.debug "Turning on thermostats" 
    Thermostat_1.setThermostatMode("state.therm_1_CurrMode")
    log.debug "$Thermostat_1 set back to $state.therm_1_CurrMode"

    if(Thermostat_2){
        Thermostat_2.setThermostatMode("state.therm_2_CurrMode")
        log.debug "$Thermostat_2 set back to $state.therm_2_CurrMode"
    }
    if(Thermostat_3){
        Thermostat_3.setThermostatMode("state.therm_3_CurrMode")
        log.debug "$Thermostat_3 set back to $state.therm_3_CurrMode"
    }
    if(Thermostat_4){
        Thermostat_4.setThermostatMode("state.therm_4_CurrMode")
        log.debug "$Thermostat_4 set back to $state.therm_4_CurrMode"
    }
    runIn(10, pollThermostats)
    runIn(20, DoubleCheck)
}
def pollThermostats(){
    if(Thermostat_1){
        if(Thermostat_1.hasCommand("poll")){
            Thermostat_1.poll()
            log.debug "polling Thermostat_1"
        }
        else if(Thermostat_1.hasCommand("refresh")){
            Thermostat_1.refresh()
            log.debug "refreshing Thermostat_1"
        }
        else { log.debug "Thermostat_1 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_2){
        if(Thermostat_2.hasCommand("poll")){
            Thermostat_2.poll()
            log.debug "polling Thermostat_2"
        }
        else if(Thermostat_2.hasCommand("refresh")){
            Thermostat_2.refresh()
            log.debug "refreshing Thermostat_2"
        }
        else { log.debug "Thermostat_2 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_3){
        if(Thermostat_3.hasCommand("poll")){
            Thermostat_3.poll()
            log.debug "polling Thermostat_3"
        }
        else if(Thermostat_3.hasCommand("refresh")){
            Thermostat_3.refresh()
            log.debug "refreshing Thermostat_3"
        }
        else { log.debug "Thermostat_2 does not support either poll() nor refresh() commands"
             }
    }
    if(Thermostat_4){
        if(Thermostat_4.hasCommand("poll")){
            Thermostat_4.poll()
            log.debug "polling Thermostat_2"
        }
        else if(Thermostat_3.hasCommand("refresh")){
            Thermostat_4.refresh()
            log.debug "refreshing Thermostat_2"
        }
        else { log.debug "Thermostat_4 does not support either poll() nor refresh() commands"
             }
    }
}
def DoubleCheck() {
    def CurrentValue1 = Thermostat_1.currentValue("thermostatMode") as String
    if(CurrentValue1 == "off"){
        Thermostat_1.setThermostatMode("auto")
        log.debug "$Thermostat_1 was set to AUTO because there was a status discrepancy"
    }
    else if(Thermostat_1.currentValue("thermostatMode")  != state.therm_1_CurrMode){
        Thermostat_1.setThermostatMode("state.therm_1_CurrMode")
        log.debug "$Thermostat_1 was not set properly. Fixing this now."
    }
    if(Thermostat_2){
        def CurrentValue2 = Thermostat_2.currentValue("thermostatMode")  as String
        if(CurrentValue2 == "off"){
            Thermostat_2.setThermostatMode("auto")
            log.debug "$Thermostat_2 was set to AUTO because there was a status discrepancy"
        }
        else if(Thermostat_2.currentValue("thermostatMode") != state.therm_2_CurrMode){
            Thermostat_2.setThermostatMode("state.therm_2_CurrMode")
            log.debug "$Thermostat_2 was not set properly. Fixing this now."
        }
    }
    if(Thermostat_3){
        def CurrentValue3 = Thermostat_3.currentValue("thermostatMode") as String
        if(CurrentValue3 == "off"){
            Thermostat_3.setThermostatMode("auto")
            log.debug "$Thermostat_3 was set to AUTO because there was a status discrepancy"
        }
        else if(Thermostat_3.currentValue("thermostatMode")  != state.therm_3_CurrMode){
            Thermostat_3.setThermostatMode("state.therm_3_CurrMode")
            log.debug "$Thermostat_3 was not set properly. Fixing this now."
        }
    }
    if(Thermostat_4){
        def CurrentValue4 = Thermostat_4.currentValue("thermostatMode") as String
        if(CurrentValue4 == "off"){
            Thermostat_4.setThermostatMode("auto")
            log.debug "$Thermostat_4 was set to AUTO because there was a status discrepancy"
        }
        else if(Thermostat_4.currentValue("thermostatMode")  != state.therm_4_CurrMode){
            Thermostat_4.setThermostatMode("state.therm_4_CurrMode")
            log.debug "$Thermostat_4 was not set properly. Fixing this now."
        }
    }
    log.debug "DOUBLE CHECK OK"
}

