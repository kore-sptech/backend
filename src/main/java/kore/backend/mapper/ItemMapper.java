package kore.backend.mapper;

import kore.backend.dto.ItemDTO;
import kore.backend.model.Item;

public final class ItemMapper {
    private ItemMapper() {
    }

    public static Item fromDto(ItemDTO itemDTO) {
        if (itemDTO == null) {
            return new Item();
        }

        Item item = new Item();
        item.setDataEntrada(itemDTO.dataEntrada());
        item.setDataValidade(itemDTO.dataValidade());
        item.setSeAtivo(itemDTO.seAtivo());
        item.setValorUnitario(itemDTO.valorUnitario());
        return item;
    }
}
