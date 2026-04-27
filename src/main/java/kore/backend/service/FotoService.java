package kore.backend.service;

import kore.backend.dto.FotoRequestDTO;
import kore.backend.model.Foto;
import kore.backend.repository.FotoRepository;
import org.springframework.stereotype.Service;

@Service
public class FotoService {

    private  final FotoRepository fotoRepository;

    public FotoService(FotoRepository fotoRepository) {
        this.fotoRepository = fotoRepository;
    }



    public Foto salvar(
            FotoRequestDTO request
    ){
        Foto foto = new Foto(
                request.imageUrl(),
                request.nome()
        );

        return this.fotoRepository.save(foto);
    }
}
