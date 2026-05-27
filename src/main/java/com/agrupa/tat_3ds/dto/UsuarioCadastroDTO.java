package com.agrupa.tat_3ds.dto;

import lombok.Data;

@Data
public class UsuarioCadastroDTO {

    private String nome;
    // Sobrenome precisa ser texto para permitir trim() e toLowerCase() no cadastro.
    private String sobrenome;
    private String hash;
}
