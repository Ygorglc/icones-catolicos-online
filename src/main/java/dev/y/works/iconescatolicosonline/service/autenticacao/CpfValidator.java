package dev.y.works.iconescatolicosonline.service.autenticacao;

public final class CpfValidator {
    private CpfValidator() {
    }

    public static boolean isValid(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) return false;
        return digit(cpf, 9) == cpf.charAt(9) - '0' && digit(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digit(String cpf, int length) {
        int sum = 0;
        for (int index = 0; index < length; index++) sum += (cpf.charAt(index) - '0') * (length + 1 - index);
        int remainder = 11 - sum % 11;
        return remainder >= 10 ? 0 : remainder;
    }
}
