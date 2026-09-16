package kore.backend.service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import kore.backend.dto.produto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.mapper.ProdutoMapper;
import kore.backend.model.Produto;
import kore.backend.repository.ProdutoRepository;
import kore.backend.service.policy.ProdutoPolicy;
import kore.backend.service.validation.CategoriaValidationService;
import kore.backend.service.validation.ProdutoValidationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final CategoriaValidationService categoriaValidationService;
    private final ProdutoValidationService produtoValidationService;
    private final ProdutoPolicy produtoPolicy;
    private final S3StorageService s3StorageService;

    public ProdutoService(ProdutoRepository produtoRepository,
            CategoriaValidationService categoriaValidationService,
            ProdutoValidationService produtoValidationService,
            ProdutoPolicy produtoPolicy,
            S3StorageService s3StorageService
        ) {
        this.produtoRepository = produtoRepository;
        this.categoriaValidationService = categoriaValidationService;
        this.produtoValidationService = produtoValidationService;
        this.produtoPolicy = produtoPolicy;
        this.s3StorageService = s3StorageService;
    }

    @Transactional
    public Produto salvarProduto(ProdutoDTO produtoDTO, Long fkUsuario) {
        produtoPolicy.validarCadastro(produtoDTO, fkUsuario);
        Produto p = ProdutoMapper.fromDto(produtoDTO);
        p.setCategoria(categoriaValidationService.obterCategoria(produtoDTO.categoriaId()));
        p.setFkUsuario(fkUsuario);
        return produtoRepository.save(p);
    }

    
    @Transactional
    public Produto salvarImagem(Long produtoId, MultipartFile arquivo) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Produto nao encontrado",
                                produtoId
                        )
                );

        try {

            String objectKey = s3StorageService.generateObjectKey(
                    produtoId,
                    arquivo.getOriginalFilename()
            );

            s3StorageService.upload(objectKey, arquivo);

            String imagemAntiga = produto.getImagemKey();

            produto.setImagemKey(objectKey);

            Produto produtoAtualizado = produtoRepository.save(produto);

            if (imagemAntiga != null && !imagemAntiga.isBlank()) {
                s3StorageService.delete(imagemAntiga);
            }

            return produtoAtualizado;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao enviar imagem para o S3",
                    e
            );
        }
    }


    public List<Produto> listarTodosProdutos(Long fkUsuario) {
        produtoPolicy.validarUsuario(fkUsuario);
        return produtoRepository.findAllByFkUsuario(fkUsuario);
    }

    @Transactional
    public Produto atualizarProduto(Long fkUsuario, Long id, ProdutoDTO produtoDTO) {
        Produto p = produtoPolicy.validarProdutoDoUsuario(id, fkUsuario);
        p.atualizarProduto(
                produtoDTO.descricao(),
                produtoDTO.nome(),
                produtoDTO.qtdMinAlerta(),
                produtoDTO.tipo());
        return produtoRepository.save(p);
    }

    @Transactional
    public void deletarProduto(Long fkUsuario, Long id) {
        Produto p = produtoPolicy.validarProdutoDoUsuario(id, fkUsuario);
        produtoRepository.delete(p);
    }
}

