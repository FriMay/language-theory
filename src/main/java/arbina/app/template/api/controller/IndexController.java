package arbina.app.template.api.controller;

import arbina.app.template.config.SwaggerConfig;
import arbina.app.template.store.entity.Sample;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import arbina.app.template.api.dto.CursoredListBodyDTO;
import arbina.app.template.api.dto.CursoredListDTO;
import arbina.app.template.api.dto.SampleDTO;
import arbina.app.template.config.ResourceServerConfig;
import arbina.app.template.store.repository.SampleRepository;

import javax.transaction.Transactional;
import java.util.stream.Stream;

@Controller
@Transactional
public class IndexController {

    private SampleRepository sampleRepository;

    public IndexController(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @ApiOperation(value = "Stub method with authorization description.",
            authorizations = { @Authorization(value = SwaggerConfig.oAuth2) })
    @GetMapping("/api")
    @Secured({
            ResourceServerConfig.OBSERVER,
    })
    public ResponseEntity<CursoredListBodyDTO<SampleDTO>> index(@RequestParam(defaultValue = "") String cursor,
                                                                @RequestParam(defaultValue = "100") Integer limit) {

        Stream<Sample> sampleStream = sampleRepository.fetchAllSortedStream();

        CursoredListDTO<Sample, SampleDTO> dto = new CursoredListDTO<>(sampleStream.iterator(),
                cursor, limit, SampleDTO::of);

        return ResponseEntity.ok(dto);
    }
}
