package org.jitsi.jibri.selenium

import org.jitsi.jibri.CallParams

/**
 * Options that can be passed to [JibriSelenium]
 */
data class JibriSeleniumOptions(
    /**
     * The parameters necessary for joining a call
     */
    val callParams: CallParams,
    /**
     * Which display selenium should be started on
     */
    val display: String = ":0",
    /**
     * The display name that should be used for jibri.  Note that this
     * is currently only used in the sipgateway gateway scenario; when doing
     * recording the jibri is 'invisible' in the call
     */
    val displayName: String = "",
    /**
     * The email that should be used for jibri.  Note that this
     * is currently only used in the sipgateway gateway scenario; when doing
     * recording the jibri is 'invisible' in the call
     */
    val email: String = ""
)
