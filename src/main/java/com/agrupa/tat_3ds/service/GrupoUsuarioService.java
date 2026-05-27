package com.agrupa.tat_3ds.service;

import com.agrupa.tat_3ds.models.Grupo;
import com.agrupa.tat_3ds.models.GrupoUsuario;
import com.agrupa.tat_3ds.models.Usuario;
import com.agrupa.tat_3ds.repository.GrupoRepository;
import com.agrupa.tat_3ds.repository.GrupoUsuarioRepository;
import com.agrupa.tat_3ds.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GrupoUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private GrupoUsuarioRepository grupoUsuarioRepository;


    @Transactional
    public GrupoUsuario moverUsuario(Integer idUsuario, Integer idGrupo, Integer posicao) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        Optional<Grupo> grupoOpt = grupoRepository.findById(idGrupo);

        if (usuarioOpt.isEmpty() || grupoOpt.isEmpty()) {
            return null;
        }

        Usuario usuario = usuarioOpt.get();
        Grupo grupoDestino = grupoOpt.get();

        if (posicao == null || posicao <= 0) {
            return null;
        }

        List<GrupoUsuario> posicoesDestino = grupoUsuarioRepository.findByGrupo(grupoDestino);

        Optional<GrupoUsuario> vagaDestinoOpt = posicoesDestino.stream()
                .filter(g -> posicao.equals(g.getPosicao()))
                .findFirst();

        if (vagaDestinoOpt.isEmpty()) {
            return null;
        }

        GrupoUsuario vagaDestino = vagaDestinoOpt.get();

        if (vagaDestino.getUsuario() != null
                && !vagaDestino.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            return null;
        }

        // Valida a vaga destino antes de liberar a posicao antiga do usuario.
        List<GrupoUsuario> existentes = grupoUsuarioRepository.findByUsuario(usuario);
        List<GrupoUsuario> existentesNoMesmoGrupo = existentes.stream()
                .filter(g -> g.getGrupo() != null
                        && g.getGrupo().getTrabalhoEmGrupo() != null
                        && g.getGrupo().getTrabalhoEmGrupo().getIdTrabalho()
                        .equals(grupoDestino.getTrabalhoEmGrupo().getIdTrabalho()))
                .toList();

        for (GrupoUsuario antigo : existentesNoMesmoGrupo) {
            if (!antigo.getIdSequencial().equals(vagaDestino.getIdSequencial())) {
                antigo.setUsuario(null);
                grupoUsuarioRepository.save(antigo);
            }
        }

        vagaDestino.setUsuario(usuario);

        usuario.setIdGrupo(grupoDestino.getIdGrupo());
        usuarioRepository.save(usuario);

        return grupoUsuarioRepository.save(vagaDestino);
    }


    @Transactional
    public int resetPosicoesAll() {

        List<GrupoUsuario> todos = grupoUsuarioRepository.findAll();

        int count = 0;

        for (GrupoUsuario gu : todos) {
            if (gu.getUsuario() != null) {
                gu.setUsuario(null);
                count++;
            }
        }

        grupoUsuarioRepository.saveAll(todos);

        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            u.setIdGrupo(null);
        }
        usuarioRepository.saveAll(usuarios);

        return count;
    }

    @Transactional
    public int resetPosicoesGrupo(Integer idGrupo) {

        Optional<Grupo> grupoOpt = grupoRepository.findById(idGrupo);

        if (grupoOpt.isEmpty()) return 0;

        List<GrupoUsuario> lista = grupoUsuarioRepository.findByGrupo(grupoOpt.get());

        int count = 0;

        for (GrupoUsuario gu : lista) {
            if (gu.getUsuario() != null) {
                gu.setUsuario(null);
                count++;
            }
        }

        grupoUsuarioRepository.saveAll(lista);

        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            if (idGrupo.equals(u.getIdGrupo())) {
                u.setIdGrupo(null);
            }
        }
        usuarioRepository.saveAll(usuarios);

        return count;
    }

}
