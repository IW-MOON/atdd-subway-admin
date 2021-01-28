package nextstep.subway.line.application;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;

@Service
@Transactional
public class LineService {
	private final LineRepository lineRepository;
	private final StationService stationService;

	public LineService(LineRepository lineRepository, StationService stationService) {
		this.lineRepository = lineRepository;
		this.stationService = stationService;
	}

	public LineResponse saveLine(LineRequest request) {
		Line line = lineRepository.save(request.toLine());

		return addSection(line, request.getUpStationId(), request.getDownStationId(), request.getDistance());
	}

	private Map<Long, Station> findStations(Long... ids) {
		return stationService.findStationsByIds(Arrays.asList(ids))
			.stream()
			.collect(Collectors.toMap(Station::getId, Function.identity()));
	}

	public LineResponse updateLine(Long id, LineRequest request) {
		Line line = findById(id);
		line.update(request.toLine());

		return LineResponse.of(line);
	}

	@Transactional(readOnly = true)
	public List<LineResponse> findAllLines() {
		List<Line> lines = lineRepository.findAll();

		return lines.stream()
			.map(LineResponse::of)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public LineResponse findLineById(Long id) {
		Line line = findById(id);

		return LineResponse.of(line);
	}

	public LineResponse addSection(Long lineId, SectionRequest request) {
		Line line = findById(lineId);

		return addSection(line, request.getUpStationId(), request.getDownStationId(), request.getDistance());
	}

	public LineResponse addSection(Line line, Long upStationId, Long downStationId, int distance) {
		Map<Long, Station> stations = findStations(upStationId, downStationId);
		Station upStation = stations.get(upStationId);
		Station downStation = stations.get(downStationId);

		line.addSection(upStation, downStation, distance);
		return LineResponse.of(line);
	}

	public void deleteLineById(Long id) {
		lineRepository.deleteById(id);
	}

	private Line findById(Long id) {
		return lineRepository.findById(id).orElseThrow(IllegalArgumentException::new);
	}
}
