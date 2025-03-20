package com.tiempo.clima.controller;

import com.tiempo.clima.dto.JwtDto;
import com.tiempo.clima.dto.LoginUsuario;
import com.tiempo.clima.dto.Mensaje;
import com.tiempo.clima.dto.NuevoUsuario;
import com.tiempo.clima.entity.Rol;
import com.tiempo.clima.entity.Usuario;
import com.tiempo.clima.security.enums.RolNombre;
import com.tiempo.clima.security.jwt.JwtProvider;
import com.tiempo.clima.service.RolService;
import com.tiempo.clima.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Tag(name = "1. Autenticación", description = "Endpoints para registrar y autenticar a los usuarios")
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthController(AuthenticationManager authenticationManager, UsuarioService usuarioService,
                          RolService rolService, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.rolService = rolService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Operation(summary = "Registrar un nuevo usuario", description = "Registra un nuevo usuario con roles específicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "El nombre de usuario o el email ya existen"),
            @ApiResponse(responseCode = "500", description = "Error interno al registrar el usuario")
    })
    @PostMapping("/nuevo")
    public ResponseEntity<?> nuevo(@Valid @RequestBody NuevoUsuario nuevoUsuario) {
        if (usuarioService.existsByNombreUsuario(nuevoUsuario.getNombreUsuario())) {
            return ResponseEntity.badRequest().body(new Mensaje("Ese nombre de usuario ya existe"));
        }
        if (usuarioService.existsByEmail(nuevoUsuario.getEmail())) {
            return ResponseEntity.badRequest().body(new Mensaje("Ese email ya existe"));
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nuevoUsuario.getNombreUsuario());
        usuario.setEmail(nuevoUsuario.getEmail());
        usuario.setPassword(passwordEncoder.encode(nuevoUsuario.getPassword()));


        Set<Rol> roles = new HashSet<>();

        if (nuevoUsuario.getRoles() == null || nuevoUsuario.getRoles().isEmpty()) {
            // usuario rol relacion 1
            Optional<Rol> userRole = rolService.getByRolNombre(RolNombre.USER);
            if (userRole.isPresent()) {
                roles.add(userRole.get());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new Mensaje("Error: El rol USER no está registrado en la base de datos"));
            }
        } else {
            // solo si esta el rol se pasa el test y se valida e imprime
            for (String rolStr : nuevoUsuario.getRoles()) {
                try {
                    RolNombre rolNombre = RolNombre.valueOf(rolStr.toUpperCase());
                    Optional<Rol> rol = rolService.getByRolNombre(rolNombre);
                    if (rol.isPresent()) {
                        roles.add(rol.get());
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new Mensaje("Error: El rol " + rolStr + " no está registrado en la base de datos"));
                    }
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new Mensaje("Error: Rol inválido - " + rolStr));
                }
            }
        }

        usuario.setRoles(roles);
        usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Mensaje("Usuario guardado"));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve un token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario autenticado correctamente"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginUsuario loginUsuario) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUsuario.getNombreUsuario(),
                        loginUsuario.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtDto(jwt));
    }
}
