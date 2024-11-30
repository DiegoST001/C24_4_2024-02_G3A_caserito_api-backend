package com.tecsup.caserito_api.paq_util;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${security.jwt.key.private}")
    private String privateKey;

    @Value("${security.jwt.user.generator}")
    private String userGenerator;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;


    //GENERADOR DE TOKEN
    public String createToken(Authentication authentication, Long userId) {
        Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

        String username = authentication.getPrincipal().toString();
        String roles = authentication.getAuthorities()
                .stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "")) // Elimina el prefijo ROLE_
                .collect(Collectors.joining(",")); // Unir roles con coma

        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject(username)
                .withClaim("userId", userId) // Agrega el ID de usuario
                .withClaim("roles", roles) // Cambiado de "authorities" a "roles"
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Usa la duración de expiración desde properties
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);

        return jwtToken;
    }


    //DESINCRIPTAR TOKEN
    public DecodedJWT validateToken(String token) {
        try {

            Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(this.userGenerator)
                    .build();

            // Verifica el token
            DecodedJWT decodedJWT = verifier.verify(token);

            // Verifica si el token está expirado
            if (decodedJWT.getExpiresAt().before(new Date())) {
                throw new JWTVerificationException("Token has expired");
            }

            return decodedJWT;
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Token invalid, not Authorized: " + exception.getMessage());
        }
    }



    public String extractUsername(DecodedJWT decodedJWT){
        return decodedJWT.getSubject();
    }

    public Long extractUserId(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("userId").asLong(); // Extrae el ID de usuario
    }

    public String extractRoles(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("roles").asString(); // Extrae los roles
    }

    //PARA OBTENER DE MANERA ESPECIFICA LOS CAMPOS
    public Claim getSpecificClaim(DecodedJWT decodedJWT, String claimName) {
        return decodedJWT.getClaim(claimName);
    }
    public Map<String, Claim> retrieveAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }

}
