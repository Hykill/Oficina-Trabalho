package projeto.oficina.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import projeto.oficina.dto.ItemServicoDTO;
import projeto.oficina.dto.ItemServicoResponseDTO;
import projeto.oficina.service.ItemServicoService;

@RestController
@RequestMapping("/itens-servico")
@RequiredArgsConstructor
public class ItemServicoController {

    private final ItemServicoService itemServicoService;

    @PostMapping
    public ResponseEntity<ItemServicoResponseDTO> cadastrar(@RequestBody ItemServicoDTO dto) {
        ItemServicoResponseDTO criado = itemServicoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<ItemServicoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(itemServicoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(itemServicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemServicoResponseDTO> atualizar(@PathVariable Long id, @RequestBody ItemServicoDTO dto) {
        return ResponseEntity.ok(itemServicoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        itemServicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
