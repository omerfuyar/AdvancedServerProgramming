package com.dsu.hello_server;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

// Sign-up, login and the token endpoints. signup, login, refresh and logout are public
// (SecurityConfig); /me needs a login.
@Tag(name = "Auth", description = "Accounts and tokens")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Create a USER account")
    @ApiResponse(responseCode = "201", description = "Created; the body has no password and no hash")
    @ApiResponse(responseCode = "400", description = "Username or password does not follow the rules")
    @ApiResponse(responseCode = "409", description = "Username is taken")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@RequestBody SignupRequest request) {
        return UserResponse.from(authService.signup(request.username(), request.password()));
    }

    @Operation(summary = "Log in and get an access token and a refresh token")
    @ApiResponse(responseCode = "200", description = "Tokens")
    @ApiResponse(responseCode = "401", description = "Wrong username or password")
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

    @Operation(summary = "Exchange a refresh token for a new access token")
    @ApiResponse(responseCode = "200", description = "New tokens; the old refresh token no longer works")
    @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired")
    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @Operation(summary = "Log out: delete the refresh token")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }

    @Operation(summary = "Who is calling")
    @ApiResponse(responseCode = "200", description = "Username and authorities")
    @ApiResponse(responseCode = "401", description = "Login required")
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        return MeResponse.from(authentication);
    }
}
