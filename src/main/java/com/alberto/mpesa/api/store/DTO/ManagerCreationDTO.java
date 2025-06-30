package com.alberto.mpesa.api.store.DTO;

import lombok.NonNull;

public record ManagerCreationDTO(
       @NonNull String name,
        @NonNull String email,
        @NonNull String password
) {
}
