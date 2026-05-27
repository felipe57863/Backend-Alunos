package com.agrupa.tat_3ds.service;


import com.agrupa.tat_3ds.dto.GrupoRelatorioDTO;
import com.agrupa.tat_3ds.dto.RelatorioTrabalhoDTO;
import com.agrupa.tat_3ds.models.Grupo;
import com.agrupa.tat_3ds.repository.GrupoRepository;
import com.agrupa.tat_3ds.repository.GrupoUsuarioRepository;
import com.agrupa.tat_3ds.repository.TrabalhoEmGrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RelatorioService {
    @Autowired private TrabalhoEmGrupoRepository trabalhoRepo;
    @Autowired private GrupoRepository grupoRepo;
    @Autowired private GrupoUsuarioRepository grupoUsuarioRepo;

    public RelatorioTrabalhoDTO buscarDadosConsolidados(Integer idTrabalho) {
        var trabalho = trabalhoRepo.findById(idTrabalho)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        List<Grupo> listaGrupos = grupoRepo.findByTrabalhoEmGrupo(trabalho);

        List<GrupoRelatorioDTO> gruposDTO = listaGrupos.stream().map(g -> {
            var posicoes = grupoUsuarioRepo.findByGrupo(g);

            List<String> nomesAlunos = posicoes
                    .stream()
                    .filter(gu -> gu.getUsuario() != null)
                    .map(gu -> gu.getUsuario().getNome())
                    .toList();

            return new GrupoRelatorioDTO(
                    g.getDescricao(),
                    nomesAlunos.size(),
                    posicoes.size(),
                    nomesAlunos
            );
        }).toList();

        String dataFormatada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss"));

        return new RelatorioTrabalhoDTO(
                trabalho.getDescricao(),
                dataFormatada,
                trabalho.getQuantidadeGrupos(),
                trabalho.getQuantidadePessoas(),
                gruposDTO
        );
    }
}
