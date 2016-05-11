/**
 *  ActionOnLock
 *
 *  Copyright 2016 Justin Hirsch
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "ActionOnLock",
    namespace: "justinh24",
    author: "Justin Hirsch",
    description: "Performs and Action based on the door locking",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "setupPage")
}

def setupPage() {
	dynamicPage(name: "setupPage", title: "Setup", install: true, uninstall: true) {
		section("Title") {
			input "theLocks","capability.lockCodes", title: "Select Locks", required: true, multiple: true
		}
        section("") {
    		def phrases = location.helloHome?.getPhrases()*.label
			if (phrases) {
		    	phrases.sort()
        		input name: "homePhrases", type: "enum", title: "Home Mode Phrase", multiple: true,required: true, options: phrases, refreshAfterSelection: true, submitOnChange: true
            }
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	unsubscribe()
	subscribe(theLocks, "lock", codeUsed)
 	log.debug "state: ${state}"
}

def codeUsed(evt) {
 
  if(evt.value == "locked") {
	  if (homePhrases) {
	      location.helloHome.execute(homePhrases)
    	}
   }
}
