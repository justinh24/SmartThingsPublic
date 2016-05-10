definition(
    name: "Perform Action On Door Lock",
    namespace: "justinh24",
    author: "Justin Hirsch",
    description: "Will set a state when the door is locked.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity@3x.png")
)

preferences{
    section("Select the door lock:") {
        input "lock1", "capability.lock", required: true
    }
   
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to", required: false) {
            input "phoneNumber", "phone", title: "Warn with text message (optional)", description: "Phone Number", required: false
        }
    }
}

def installed(){
    initialize()
}

def updated(){
    unsubscribe()
    unschedule()
    initialize()
}

def initialize(){
    log.debug "Settings: ${settings}"
    subscribe(lock1, "lock", doorHandler, [filterEvents: false])
    subscribe(lock1, "unlock", doorHandler, [filterEvents: false])  
    subscribe(contact, "contact.open", doorHandler)
    subscribe(contact, "contact.closed", doorHandler)
}

def lockDoor(){
    log.debug "Locking the door."
    lock1.lock()
    if(location.contactBookEnabled) {
        if ( recipients ) {
            log.debug ( "Sending Push Notification..." ) 
            sendNotificationToContacts( "${lock1} locked after ${contact} was closed for ${minutesLater} minutes!", recipients)
        }
    }
    if (phoneNumber) {
        log.debug("Sending text message...")
        sendSms( phoneNumber, "${lock1} locked after ${contact} was closed for ${minutesLater} minutes!")
    }
}

def unlockDoor(){
    log.debug "Unlocking the door."
    lock1.unlock()
    if(location.contactBookEnabled) {
        if ( recipients ) {
            log.debug ( "Sending Push Notification..." ) 
            sendNotificationToContacts( "${lock1} unlocked after ${contact} was opened for ${secondsLater} seconds!", recipients)
        }
    }
    if ( phoneNumber ) {
        log.debug("Sending text message...")
        sendSms( phoneNumber, "${lock1} unlocked after ${contact} was opened for ${secondsLater} seconds!")
    }
}

def doorHandler(evt){
    if ((contact.latestValue("contact") == "open") && (evt.value == "locked")) { // If the door is open and a person locks the door then...  
        //def delay = (secondsLater) // runIn uses seconds
        runIn( secondsLater, unlockDoor )   // ...schedule (in minutes) to unlock...  We don't want the door to be closed while the lock is engaged. 
    }
    else if ((contact.latestValue("contact") == "open") && (evt.value == "unlocked")) { // If the door is open and a person unlocks it then...
        unschedule( unlockDoor ) // ...we don't need to unlock it later.
    }
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "locked")) { // If the door is closed and a person manually locks it then...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }   
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "unlocked")) { // If the door is closed and a person unlocks it then...
       //def delay = (minutesLater * 60) // runIn uses seconds
        runIn( (minutesLater * 60), lockDoor ) // ...schedule (in minutes) to lock.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "open")) { // If a person opens an unlocked door...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "closed")) { // If a person closes an unlocked door...
        //def delay = (minutesLater * 60) // runIn uses seconds
        runIn( (minutesLater * 60), lockDoor ) // ...schedule (in minutes) to lock.
    }
    else { //Opening or Closing door when locked (in case you have a handle lock)
        log.debug "Unlocking the door."
        lock1.unlock()
        if(location.contactBookEnabled) {
            if ( recipients ) {
                log.debug ( "Sending Push Notification..." ) 
                sendNotificationToContacts( "${lock1} unlocked after ${contact} was opened or closed when ${lock1} was locked!", recipients)
            }
        }
        if ( phoneNumber ) {
            log.debug("Sending text message...")
            sendSms( phoneNumber, "${lock1} unlocked after ${contact} was opened or closed when ${lock1} was locked!")
        }
    }
}