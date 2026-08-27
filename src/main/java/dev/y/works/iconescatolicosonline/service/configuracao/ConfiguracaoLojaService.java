package dev.y.works.iconescatolicosonline.service.configuracao;

import dev.y.works.iconescatolicosonline.domain.configuracao.ConfiguracaoLoja;
import dev.y.works.iconescatolicosonline.dto.configuracao.ConfiguracaoLojaRequest;
import dev.y.works.iconescatolicosonline.dto.configuracao.ConfiguracaoLojaResponse;
import dev.y.works.iconescatolicosonline.repository.configuracao.ConfiguracaoLojaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracaoLojaService {
    private static final long ID = 1L;
    private final ConfiguracaoLojaRepository repository;

    public ConfiguracaoLojaService(ConfiguracaoLojaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoLojaResponse buscar() {
        return paraResponse(repository.findById(ID).orElseGet(this::padrao));
    }

    @Transactional
    public ConfiguracaoLojaResponse atualizar(ConfiguracaoLojaRequest request) {
        ConfiguracaoLoja configuracao = repository.findById(ID).orElseGet(this::padrao);
        configuracao.setEntregaHabilitada(request.entregaHabilitada());
        configuracao.setChavePix(normalizar(request.chavePix()));
        configuracao.setDadosDeposito(normalizar(request.dadosDeposito()));
        return paraResponse(repository.save(configuracao));
    }

    private ConfiguracaoLoja padrao() {
        ConfiguracaoLoja configuracao = new ConfiguracaoLoja();
        configuracao.setId(ID);
        configuracao.setEntregaHabilitada(true);
        return configuracao;
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private ConfiguracaoLojaResponse paraResponse(ConfiguracaoLoja configuracao) {
        return new ConfiguracaoLojaResponse(configuracao.isEntregaHabilitada(),
                configuracao.getChavePix(), configuracao.getDadosDeposito());
    }
}
