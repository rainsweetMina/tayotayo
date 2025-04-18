package kroryi.bus2.entity.route;

public enum RouteType {
    순환 {
        @Override
        public boolean matches(String routeNo) {
            return routeNo.startsWith("순환");
        }
    },
    급행 {
        @Override
        public boolean matches(String routeNo) {
            return routeNo.startsWith("급행");
        }
    },
    간선 {
        @Override
        public boolean matches(String routeNo) {
            return routeNo.matches("^\\d+$");
        }
    },
    지선 {
        @Override
        public boolean matches(String routeNo) {
            return routeNo.matches("^[^\\d]+\\d+$");
        }
    };

    public abstract boolean matches(String routeNo);
}
