package top.linrty.netdisk.user.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.util.CalculatorUtils;
import top.linrty.netdisk.user.config.JwtProperties;
import top.linrty.netdisk.user.domain.po.UserBean;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtTool {

	@Resource
	JwtProperties jwtProperties;


	// 由字符串生成加密key
	private SecretKey generalKey() {
		// 本地的密码解码
		byte[] encodedKey = Base64.decode(jwtProperties.getSecret());
		// 根据给定的字节数组使用AES加密算法构造一个密钥
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
	}
 
	// 创建jwt
	public String createJWT(Map<String, Object> param) {
		String subject = JSON.toJSONString(param);
		// 生成JWT的时间
		long nowTime = System.currentTimeMillis();
		Date nowDate = new Date(nowTime);
		// 生成签名的时候使用的秘钥secret，切记这个秘钥不能外露，是你服务端的私钥，在任何场景都不应该流露出去，一旦客户端得知这个secret，那就意味着客户端是可以自我签发jwt的
		SecretKey key = generalKey();
		Double expireTime = CalculatorUtils.conversion(jwtProperties.getPayload().getRegisterdClaims().getExp());

		// 为payload添加各种标准声明和私有声明
		DefaultClaims defaultClaims = new DefaultClaims();
		defaultClaims.setIssuer(jwtProperties.getPayload().getRegisterdClaims().getIss());
		defaultClaims.setExpiration(new Date(System.currentTimeMillis() + expireTime.longValue()));
		defaultClaims.setSubject(subject);
		defaultClaims.setAudience(jwtProperties.getPayload().getRegisterdClaims().getAud());

		JwtBuilder builder = Jwts.builder() // 表示new一个JwtBuilder，设置jwt的body
				.setClaims(defaultClaims)
				.setIssuedAt(nowDate) // iat(issuedAt)：jwt的签发时间
				.signWith(SignatureAlgorithm.forName(jwtProperties.getHeader().getAlg()), key); // 设置签名，使用的是签名算法和签名使用的秘钥

		return builder.compact();
	}
 
	// 解密jwt
	public Claims parseJWT(String jwt) throws Exception {
		SecretKey key = generalKey(); // 签名秘钥，和生成的签名的秘钥一模一样
        return Jwts.parser() // 得到DefaultJwtParser
                .setSigningKey(key) // 设置签名的秘钥
                .parseClaimsJws(jwt).getBody();
	}

	public String getUserIdByToken(String token) {
		Claims c = null;
		if (StrUtil.isEmpty(token)) {
			return null;
		}
		token = token.replace("Bearer ", "");
		token = token.replace("Bearer%20", "");
		try {
			c = parseJWT(token);
		} catch (Exception e) {
			log.error("解码异常:" + e);
			return null;
		}
		if (c == null) {
			log.info("解码为空");
			return null;
		}
		String subject = c.getSubject();
		log.debug("解析结果：" + subject);
		UserBean tokenUserBean = JSON.parseObject(subject, UserBean.class);
		return tokenUserBean.getUserId();
	}

 
}