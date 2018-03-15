package org.jitsi.jibri.selenium.pageobjects

import org.jitsi.jibri.CallUrlInfo
import org.jitsi.jibri.util.extensions.error
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.WebDriverWait
import java.util.logging.Logger

/**
 * Page object representing the in-call page on a jitsi-meet server
 */
class CallPage(driver: RemoteWebDriver) : AbstractPageObject(driver) {
    private val logger = Logger.getLogger(this::class.qualifiedName)
    @FindBy(xpath = "//*[@id=\"toolbar_button_hangup\"]")
    private val hangUpButton: WebElement? = null

    init {
        PageFactory.initElements(driver, this)
    }

    override fun visit(callUrlInfo: CallUrlInfo): Boolean {
        if (!super.visit(callUrlInfo)) {
            return false
        }
        val start = System.currentTimeMillis()
        return try {
            WebDriverWait(driver, 30).until {
                driver.executeScript("return typeof(APP.conference && APP.conference._room) !== 'undefined'")
            }
            val totalTime = System.currentTimeMillis() - start
            logger.info("Waited $totalTime milliseconds for call page to load")
            true
        } catch (t: TimeoutException) {
            logger.error("Timed out waiting for call page to load")
            false
        }
    }

    fun getNumParticipants(driver: RemoteWebDriver): Long {
        val result = driver.executeScript("""
            try {
                return APP.conference.membersCount;
            } catch (e) {
                return e.message;
            }
        """.trimMargin())
        return when (result) {
            is Long -> result
            else -> 1
        }
    }

    fun injectParticipantTrackerScript(driver: RemoteWebDriver): Boolean {
        val result = driver.executeScript("""
            try {
                window._jibriParticipants = [];
                APP.conference._room.room.addListener(
                    "xmpp.muc_member_joined",
                    (from, nick, role, hidden, statsid, status) => {
                        window._jibriParticipants.push(from);
                    }
                );
                return true;
            } catch (e) {
                return e.message;
            }
        """.trimMargin())
        return when (result) {
            is Boolean -> result
            else -> false
        }
    }

    fun getParticipants(driver: RemoteWebDriver): List<String> {
        val result = driver.executeScript("""
            try {
                return window._jibriParticipants;
            } catch (e) {
                return e.message;
            }
        """.trimMargin())
        if (result is List<*>) {
            @Suppress("UNCHECKED_CAST")
            return result as List<String>
        } else {
            return listOf()
        }
    }

    fun leave() {
        hangUpButton?.click()
        //TODO: wait for call to be fully exited?
    }
}
