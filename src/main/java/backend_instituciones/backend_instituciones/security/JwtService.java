package backend_instituciones.backend_instituciones.security;

/**
 * @deprecated Replaced by Supabase JWT validation via spring-boot-starter-oauth2-resource-server.
 * WebSocketHandshakeInterceptor and WebSocketChannelInterceptor now use JwtDecoder directly.
 * Safe to delete once all @Autowired references to JwtService are removed.
 */
@Deprecated
public class JwtService {
}
