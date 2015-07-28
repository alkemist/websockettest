import com.entero.websocket.LogFilePublisher
import com.fasterxml.jackson.databind.ObjectMapper
import ratpack.func.Function

import java.time.Duration

import static ratpack.groovy.Groovy.ratpack
import static ratpack.stream.Streams.periodically
import static ratpack.websocket.WebSockets.websocketBroadcast

ratpack {
    serverConfig {
        development(true)
    }

    bindings {
        bind(LogFilePublisher)
    }

    handlers {
        get() {
            redirect("index.html")
        }

        get("ws") { ctx ->
            websocketBroadcast(ctx, ctx.get(LogFilePublisher))
        }

        get("ws2") { ctx ->
            websocketBroadcast(context, periodically(registry, Duration.ofMillis(1000),
                    new Function<Integer, String>() {
                        @Override
                        public String apply(Integer i) throws Exception {
                            return new ObjectMapper().writeValueAsString(
                                    [
                                            message: i
                                    ])
                        }
                    })
            )
        }

        assets "public"
    }
}
