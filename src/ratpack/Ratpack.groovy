import com.entero.websocket.LoggingService
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
        bindInstance(new LoggingService())
    }

    handlers {
        get {
            redirect "index.html"
        }

        get("ws") { LoggingService loggingService ->
            websocketBroadcast(context, loggingService.publisher)
        }

        get("ws2") {
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
