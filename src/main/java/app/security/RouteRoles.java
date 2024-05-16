package app.security;

    public enum RouteRoles implements io.javalin.security.RouteRole {
        ANYONE("anyone"), USER("user"), ADMIN("admin"), INSTRUCTOR("instructor");

        private final String role;

        RouteRoles(String role) {
            this.role = role;
        }

        @Override
        public String toString() {
            return role;
        }
    }

