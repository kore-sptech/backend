package kore.backend.mapper;

import kore.backend.dto.TransacaoDTO;
import kore.backend.model.Transacao;

public final class TransacaoMapper {
    private TransacaoMapper() {
    }

    public static Transacao fromDto(TransacaoDTO dto) {
        if (dto == null) {
            return new Transacao();
        }

        Transacao transacao = new Transacao();
        transacao.setValor(dto.valor());
        transacao.setNome(dto.nome());
        transacao.setTipo(dto.tipo());
        transacao.setCategoria(dto.categoria());
        return transacao;
    }
}
