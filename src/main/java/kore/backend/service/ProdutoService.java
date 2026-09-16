package kore.backend.service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import kore.backend.dto.produto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.CategoriaRepository;
import kore.backend.repository.ProdutoRepository;
import kore.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final S3StorageService s3StorageService;

    public ProdutoService(ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository, S3StorageService s3StorageService) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.s3StorageService = s3StorageService;
    }

    @Transactional
    public Produto salvarProduto(ProdutoDTO produtoDTO) {
        if(usuarioRepository.existsById(produtoDTO.usuario())){
            Produto p = new Produto(produtoDTO);
            if (categoriaRepository.existsById(produtoDTO.categoriaId())){
                p.setCategoria(categoriaRepository.findById(produtoDTO.categoriaId())
                        .orElseThrow(() -> new EntityExistsException("Id da categoria não existe"))
                );
            }
            return produtoRepository.save(p);
        }
        throw new RecursoNaoEncontradoException("Usuario nao encontrado", produtoDTO.usuario());
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

    public List<Produto> listarTodosProdutos() {
        return produtoRepository.findAll();
    }

    @Transactional
    public Produto atualizarProduto(Long id, ProdutoDTO produtoDTO) {
        Produto p = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado", id));
        p.setDescricao(produtoDTO.descricao());
        p.setNome(produtoDTO.nome());
        p.setQtdMinAlerta(produtoDTO.qtdMinAlerta());
        p.setTipo(produtoDTO.tipo());
        return produtoRepository.save(p);
    }

    @Transactional
    public void deletarProduto(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado", id);
        }
        produtoRepository.deleteById(id);
    }
}

