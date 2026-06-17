package ee.joeltek.match_me.chat;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {}