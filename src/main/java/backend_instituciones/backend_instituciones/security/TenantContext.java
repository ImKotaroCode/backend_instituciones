package backend_instituciones.backend_instituciones.security;

public class TenantContext {

    private static final ThreadLocal<Long> INSTITUTION_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setInstitutionId(Long institutionId) {
        INSTITUTION_ID.set(institutionId);
    }

    public static Long getInstitutionId() {
        return INSTITUTION_ID.get();
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        INSTITUTION_ID.remove();
        USER_ID.remove();
    }
}
