package com.agrupa.tat_3ds.service;

import com.agrupa.tat_3ds.dto.GrupoInformacoesDTO;

import com.agrupa.tat_3ds.dto.PosicaoUsuariosDTO;
import com.agrupa.tat_3ds.models.Grupo;
import com.agrupa.tat_3ds.models.GrupoUsuario;
import com.agrupa.tat_3ds.repository.GrupoRepository;
import com.agrupa.tat_3ds.repository.GrupoUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GrupoInformacoesService {

    @Autowired
    private GrupoRepository grupoRepo;

    @Autowired
    private GrupoUsuarioRepository grupoUsuarioRepo;

    public GrupoInformacoesDTO getInformacoes(Integer idGrupo) {
        Grupo grupo = grupoRepo.findById(idGrupo)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado!"));
        return mapToDTO(grupo);
    }

    public List<GrupoInformacoesDTO> getInformacoesGrupos() {
        return grupoRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private GrupoInformacoesDTO mapToDTO(Grupo grupo) {
        List<GrupoUsuario> grupoUsuarios = grupoUsuarioRepo.findByGrupo(grupo);

        int capacidadePelaPosicao = grupoUsuarios.stream()
                .mapToInt(GrupoUsuario::getPosicao)
                .max()
                .orElse(0);

        // A quantidade de usuarios considera apenas posicoes ocupadas.
        int quantidadeUsuarios = (int) grupoUsuarios.stream()
                .filter(p -> p.getUsuario() != null)
                .count();

        List<PosicaoUsuariosDTO> posicaoUsuarios = grupoUsuarios.stream()
                .map(p -> new PosicaoUsuariosDTO(p.getPosicao(),
                        p.getUsuario() != null ? p.getUsuario().getNome() : null))
                .collect(Collectors.toList());

        var trabalho = grupo.getTrabalhoEmGrupo();

        return new GrupoInformacoesDTO(
                grupo.getIdGrupo(),
                posicaoUsuarios,
                grupo.getDescricao(),
                quantidadeUsuarios,
                capacidadePelaPosicao,
                trabalho.getHashParaAcesso(),
                trabalho.getDescricao(),
                trabalho.getIdTrabalho()
        );
    }
}
