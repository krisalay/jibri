package org.jitsi.jibri.api.http

import org.jitsi.jibri.CallParams
import org.jitsi.jibri.FileRecordingParams
import org.jitsi.jibri.JibriManager
import org.jitsi.jibri.RecordingSinkType
import org.jitsi.jibri.ServiceParams
import org.jitsi.jibri.StartServiceResult
import org.jitsi.jibri.StreamingParams
import org.jitsi.jibri.service.impl.SipGatewayServiceParams
import org.jitsi.jibri.sipgateway.SipClientParams
import org.jitsi.jibri.util.extensions.debug
import org.jitsi.jibri.util.extensions.error
import java.util.logging.Logger
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

//TODO(brian): i had to put default values here or else jackson would fail
// to parse it as part of a request, though from what i've read this
// shouldn't be necessary?
// https://github.com/FasterXML/jackson-module-kotlin
// https://craftsmen.nl/kotlin-create-rest-services-using-jersey-and-jackson/
data class StartServiceParams(
    val callParams: CallParams = CallParams(),
    val sinkType: RecordingSinkType = RecordingSinkType.FILE,
    //TODO: change this to String? and default to null.  make sure a missing stream
    // key is handled correctly
    val youTubeStreamKey: String = "",
    /**
     * Params to be used if [RecordingSinkType] is [RecordingSinkType.GATEWAY]
     */
    val sipClientParams: SipClientParams? = null
)

/**
 * The [HttpApi] is for starting and stopping the various Jibri services via the
 * [JibriManager], as well as retrieving the health and status of this Jibri
 */
@Path("/jibri/api/v1.0")
class HttpApi(private val jibriManager: JibriManager) {
    private val logger = Logger.getLogger(this::class.qualifiedName)

    /**
     * Get the health of this Jibri in the format of a json-encoded
     * [JibriHealth] object
     */
    @GET
    @Path("health")
    @Produces(MediaType.APPLICATION_JSON)
    fun health(): Response {
        logger.debug("Got health request")
        return Response.ok(jibriManager.healthCheck()).build()
    }

    /**
     * [startService] will start a new service using the given [StartServiceParams].
     * Returns a response with [Response.Status.OK] on success, [Response.Status.PRECONDITION_FAILED]
     * if this Jibri is already busy and [Response.Status.INTERNAL_SERVER_ERROR] on error
     */
    @POST
    @Path("startService")
    @Consumes(MediaType.APPLICATION_JSON)
    fun startService(startServiceParams: StartServiceParams): Response {
        logger.debug("Got a start service request with params $startServiceParams")
        val result: StartServiceResult = when (startServiceParams.sinkType) {
            RecordingSinkType.FILE -> {
                jibriManager.startFileRecording(
                    ServiceParams(usageTimeoutMinutes = 0),
                    FileRecordingParams(startServiceParams.callParams)
                )
            }
            RecordingSinkType.STREAM -> {
                jibriManager.startStreaming(
                    ServiceParams(usageTimeoutMinutes = 0),
                    StreamingParams(startServiceParams.callParams, startServiceParams.youTubeStreamKey)
                )
            }
            RecordingSinkType.GATEWAY -> {
                startServiceParams.sipClientParams?.let {
                    jibriManager.startSipGateway(
                        ServiceParams(usageTimeoutMinutes = 0),
                        SipGatewayServiceParams(
                            startServiceParams.callParams,
                            it)
                    )
                } ?: run {
                    logger.error("SipGatewayService requested, but no SIP params passed")
                    StartServiceResult.ERROR
                }
            }
        }
        return when (result) {
            StartServiceResult.SUCCESS -> Response.ok().build()
            StartServiceResult.BUSY -> Response.status(Response.Status.PRECONDITION_FAILED).build()
            StartServiceResult.ERROR -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * [stopService] will stop the current service immediately
     */
    @POST
    @Path("stopService")
    fun stopService(): Response {
        logger.debug("Got stop service request")
        jibriManager.stopService()
        return Response.ok().build()
    }
}
