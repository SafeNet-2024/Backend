package com.SafeNet.Backend.global.auth;

import ch.qos.logback.core.status.Status;
import com.SafeNet.Backend.domain.member.dto.TokenResponseDto;
import com.SafeNet.Backend.domain.member.service.UserDetailsServiceImpl;
import com.SafeNet.Backend.global.exception.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jshell.Snippet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
@Component
@Slf4j
public class JwtTokenProvider {
    private final RedisTemplate<String, String> tokenRedisTemplate;
    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    //AccessToken 유효기한 : 1시간
    private long accessExpirationTime =  1000 * 60 * 60;
    //RefreshToken 유효기한 : 1주
    private long refreshExpirationTime = 1000 * 60 * 60 * 24 * 7;;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public JwtTokenProvider(@Qualifier("tokenRedisTemplate") RedisTemplate<String, String> tokenRedisTemplate) {
        this.tokenRedisTemplate = tokenRedisTemplate;
    }

    // bean으로 등록 되면서 딱 한번 실행
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }
/*
     * Access 토큰 생성
     * 유저 정보를 통해 AccessToken생성
     * @param [Authentication 인증 정보 객체]
     * @return [LoginResponseDto]
*/
    public String createAccessToken(Authentication authentication){
        log.info("getName() 설정 값 확인: "+ authentication.getName());
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpirationTime);


        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

/*
     * Refresh 토큰 생성
   */

    public String createRefreshToken(Authentication authentication){
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpirationTime);

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // redis에 저장
        tokenRedisTemplate.opsForValue().set(
                authentication.getName(),
                refreshToken,
                refreshExpirationTime,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

/*
     * 토큰으로부터 클레임을 만들고, 이를 통해 User 객체 생성해 Authentication 객체 반환

*/
    public Authentication getAuthentication(String accesstoken) {
        String userPrincipal = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accesstoken)
                .getBody()
                .getSubject();//유저이메일 추출
        UserDetails userDetails = userDetailsService.loadUserByUsername(userPrincipal);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

/*
     * http 헤더로부터 bearer 토큰을 가져옴.

 */

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("ACCESS_TOKEN");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

/*
     * Access 토큰을 검증
     */

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch(ExpiredJwtException e) {
            log.error("EXPIRED_JWT" +e.getMessage());
            throw new CustomException("EXPIRED_JWT");
        } catch(JwtException e) {
            log.error("INVALID_JWT"+ e.getMessage());
            throw new CustomException("INVALID_JWT");
        }
    }
    /*
     * 모든 토큰 헤더 설정
     */
    public void setHeaderToken(HttpServletResponse response, TokenResponseDto dto) {
        response.setHeader("Access_Token", dto.getAccessToken());
        response.setHeader("Refresh_Token", dto.getRefreshToken());
    }

    /*
     * 로그아웃 로직 - Refresh 토큰을 redis에서 삭제
     */
    public ResponseEntity<Status> logout(String email, String token) {
        String atk= token.substring(7);
        Long expiration = getExpiration(atk);
        if(tokenRedisTemplate.opsForValue().get(email) != null) {
            tokenRedisTemplate.delete(email);
        }
        // [ 블랙리스트 생성 단계 ] redis에 가져온 key(JWT 토큰) : value("logout")으로 저장
        tokenRedisTemplate.opsForValue().set(atk, "logout", Duration.ofMillis(expiration));

        return new ResponseEntity<>(HttpStatus.OK);
    }
    /*
     * 유효기간 가져오기
     */
    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }
}
