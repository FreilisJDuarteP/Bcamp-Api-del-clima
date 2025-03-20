
# API del Clima
API para obtener información sobre el clima actual, calidad del aire y pronóstico del tiempo.

## Instalación y Configuración
### 1. **Clonar el repositorio:**
```bash
git clone https://github.com/FreilisJDuarteP/Bcamp-Api-del-clima.git
```
### Nota: Salta al paso 3 para probar la configuración automática del proyecto.

### 2. **Configuración de la base de datos:**
Si deseas probar la API sin cambios, la base de datos ya está configurada en el contenedor.  
Para configuración local, modifica `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/apiclima?createDatabaseIfNotExist=true  
spring.datasource.username={NombreDeUsuario}  
spring.datasource.password={Contraseña}  
openweathermap.api.key={ApiKeyDeOpenWeatherMap}
```

Para construir el proyecto manualmente:
```bash
./mvnw clean package -DskipTests
```

### 3. **Levantar el contenedor (Docker):**
```bash
docker-compose up --build o docker compose up --build
```

---

## Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/nuevo` | Registrar un nuevo usuario |
| POST | `/auth/login` | Iniciar sesión |

---

## Clima
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/clima/ciudad/{nombreCiudad}` | Obtener clima de una ciudad |

---

## Contaminación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/contaminacion/ciudad/{nombreCiudad}` | Obtener calidad del aire por ciudad |

---

## Pronóstico
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/pronostico/{ciudad}` | Obtener pronóstico del clima (5 días) |

---

## Consultas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/consultas/miconsulta` | Consultas del usuario autenticado |
| GET | `/consultas/todas` | Consultas de todos los usuarios (solo admin) |

---

##  Caché
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| DELETE | `/cache/limpiar` | Limpiar la caché |

---

##  Probar Endpoints
Acceda a Swagger:
```bash
http://localhost:8080/swagger-ui.html
```
